package com.example.task2

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.IOUtils
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var mDrive: Drive

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        Toast.makeText(this@MainActivity,
            "Signed in to \n"+auth.currentUser?.email,
            Toast.LENGTH_SHORT).show()

        mDrive = getDriveService(this)
        val btnUpload = findViewById<Button>(R.id.buttonUpload)

        btnUpload.setOnClickListener(){
            val localFilePath = "storage/emulated/0/Download/output.csv"
            if (isFileExists(localFilePath)) {
                GlobalScope.async(Dispatchers.IO){
                    uploadFileToGDrive(this@MainActivity,
                        "storage/emulated/0/Download",
                        "output.csv", "outputMobiSecLab")
                    Toast.makeText(this@MainActivity, "Output file saved on drive", Toast.LENGTH_SHORT).show()
                }
            } else {
                val fileUrl = "https://raw.githubusercontent.com/MobiSec-CSE-UTA/EyeQoE_Dataset/main/0/010.csv"
                val fileName = "data.csv"

                downloadFile(applicationContext, fileUrl, fileName)

                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        // This method will be executed once the timer is over
                        GlobalScope.async(Dispatchers.IO){
                            uploadFileToGDrive(this@MainActivity,
                                "storage/emulated/0/Download",
                                "data.csv", "dataMobiSecLab")
                        }
                    },
                    2000 // value in milliseconds
                )
                Toast.makeText(this@MainActivity,
                    "Output file not found \nData file saved on drive", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadFile(context: Context, fileUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    fun isFileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.isFile
    }

    fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()
        }
        var tempDrive: Drive
        return tempDrive
    }

    fun uploadFileToGDrive(context: Context, filePath: String, fileName: String, gFileName: String) {
        mDrive.let { googleDriveService ->
            GlobalScope.launch {
                try {
                    val raunit = File(filePath, fileName)
                    val gfile = com.google.api.services.drive.model.File()
                    gfile.name = gFileName
                    val mimetype = "text/plain"
                    val fileContent = FileContent(mimetype, raunit)

                    withContext(Dispatchers.Main) {
                        withContext(Dispatchers.IO) {
                            launch {
                                var mFile =
                                    googleDriveService.Files().create(gfile, fileContent).execute()
                            }
                        }
                    }

                } catch (userAuthEx: UserRecoverableAuthIOException) {
                    Toast.makeText(
                        context,
                        "Please grant storage permission to manage all files",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(
                        userAuthEx.intent
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("asdf", e.toString())
                    Toast.makeText(
                        context,
                        "Please grant storage permission to manage all files",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data!!.data //The uri with the location of the file
            makeCopy(selectedFile!!)
            Toast.makeText(this,selectedFile.toString(),Toast.LENGTH_LONG).show()
        }
    }

    private fun makeCopy(fileUri: Uri) {
        val parcelFileDescriptor = applicationContext.contentResolver.openFileDescriptor(fileUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file = File(applicationContext.filesDir, getFileName(applicationContext.contentResolver, fileUri))
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)

    }

    private fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String {

        var name = ""
        val returnCursor = contentResolver.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

}