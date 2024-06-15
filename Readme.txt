Project Overview

GazeMotion LSTM is an Android application designed to read time-series data, encode it into a compressed embedding using a lightweight LSTM model, and save the output in a specified format. This project involves three main tasks:

	1.	LSTM Time-Series Compression
	2.	Data Uploading to Google Drive
	3.	Acceleration Data Display

Task 1: LSTM Time-Series Compression

Description

The Android app reads a time-series data file (010.csv), extracts the gaze movement data, and uses a TensorFlow Lite LSTM model to compress the data into a fixed-length vector. The compressed data is saved as an output file (output.xxx).

Key Features

	•	Reads gaze movement data from 010.csv
	•	Utilizes TensorFlow Lite for LSTM model
	•	Outputs compressed data in various formats (CSV, TXT, XLS)

Files

	•	model.tflite: Pre-initialized LSTM model
	•	output.xxx: Compressed data file

How to Run

	1.	Press the button in the app to start the compression process.
	2.	The output file will be saved in the same directory as the input file.

Task 2: Data Uploading

Description

This Android app uploads the target file (either output.xxx or 010.csv) to Google Drive when a button is pressed.

Key Features

	•	Integrates with Google Drive API
	•	Uploads files generated from Task 1

How to Run

	1.	Press the button in the app to upload the file to Google Drive.
	2.	Ensure you have the appropriate API key and permissions set up.

Task 3: Acceleration Data Display

Description

This Unity-based Android app continuously measures and displays the smartphone’s acceleration data in three axes using its IMU sensor.

Key Features

	•	Real-time acceleration data display
	•	3D visualization using Unity

How to Run

	1.	Open the app to start measuring and displaying acceleration data.
	2.	The data will be displayed in a 3D format on the screen.

Deliverables

	1.	Android app package for each task
	2.	Unity project package for Task 3
	3.	README.txt file with necessary instructions
