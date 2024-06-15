package com.example.task1

import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.random.Random
import android.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var assetManager: AssetManager
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPredict = findViewById<Button>(R.id.buttonPredict)

        // Check for write external storage permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission if it has not been granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, go ahead
        }

        btnPredict.setOnClickListener {
            Toast.makeText(this@MainActivity,"Reading...",Toast.LENGTH_SHORT).show()

            // Obtain AssetManager from the context
            assetManager = applicationContext.assets
            val csvValues = readCSV(assetManager)
//            predictOutput(assetManager, csvValues)
            Toast.makeText(this@MainActivity,"Reading finished!",Toast.LENGTH_SHORT).show()

            generateAndSaveCsv()
            Toast.makeText(this@MainActivity,"Output saved in Downloads",Toast.LENGTH_SHORT).show()

        }
    }

    // Function to read CSV file and extract desired columns
    private fun readCSV(assetManager: AssetManager): List<Pair<Float, Float>> {
        val inputStream = assetManager.open("data.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Skip the first row (column names)
        reader.readLine()
        val values = mutableListOf<Pair<Float, Float>>()
        var line = reader.readLine()
        var count = 0
        while (line != null) {
            count += 1
            val columns = line.split(",")
            // Extract 5th and 6th columns
            val column5 = columns[4].toFloat()
            val column6 = columns[5].toFloat()

            values.add(Pair(column5, column6))
            line = reader.readLine()
        }

        reader.close()

        return values
    }

    // Function to load the TensorFlow Lite model
    private fun loadModel(assetManager: AssetManager): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun predictOutput(assetManager: AssetManager, csvValues: List<Pair<Float, Float>>) {
        try {
            // Load the TensorFlow Lite model
            val tfliteModel = loadModel(assetManager)

            // Create an instance of the TensorFlow Lite interpreter
            val interpreter = Interpreter(tfliteModel)

            // Prepare input and output tensors
            val inputShape = interpreter.getInputTensor(0).shape()
            val outputShape = interpreter.getOutputTensor(0).shape()
            val inputBuffer = ByteBuffer.allocateDirect(inputShape[0] * inputShape[1] * 4) // Assuming float32 input
            val outputBuffer = ByteBuffer.allocateDirect(outputShape[0] * outputShape[1] * 4) // Assuming float32 output

// //   TOD0() need to debug this ----
/*
            // Process each value in the CSV list
            for (value in csvValues) {

                // Set the input values in the input buffer
                val column5 = value.first
                val column6 = value.second
                inputBuffer.putFloat(column5)
                inputBuffer.putFloat(column6)
                // Run inference
                inputBuffer.rewind()
                outputBuffer.rewind()
                Log.d("Testing Logs", "2222222 $inputBuffer")
                interpreter.run(inputBuffer, outputBuffer)
                // Process the output values
                val outputValues = mutableListOf<Float>()
                for (i in 0 until outputBuffer.capacity() / 4) { // Assuming float32 output
                    val value = outputBuffer.getFloat(i * 4)
                    outputValues.add(value)
                    Log.d("Output Values", "Output $i: $value")
                }
                Log.d("Testing Logs", "444444 $inputBuffer")
            }
*/
            // Clean up resources
            interpreter.close()
        } catch (e: Exception) {
            Log.d("---error---", e.toString())
            Toast.makeText(this@MainActivity,"Error POS 1",Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun generateAndSaveCsv() {
        val randomNumbers = mutableListOf<String>()
        for (i in 1..50) {
            val randomNumber = Random.nextDouble().toString()
            randomNumbers.add(randomNumber)
        }

        val csvFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "output.csv"
        )

        try {
            FileWriter(csvFile).use { writer ->
                for (number in randomNumbers) {
                    writer.append(number)
                    writer.append("\n")
                }
            }
            // CSV file is successfully created and saved
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, generate and save the CSV file
                generateAndSaveCsv()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

}