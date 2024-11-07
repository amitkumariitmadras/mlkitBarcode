//package com.google.mlkit.md
//
//import android.net.Uri
//import android.os.Bundle
//import android.util.Log
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import android.widget.MediaController
//
//class BarcodeSearchActivity : AppCompatActivity() {
//
//    private lateinit var videoView: VideoView
//    private lateinit var barcodeInput: EditText
//    private lateinit var searchButton: Button
//
//    private var videoUri: Uri? = null
//    private var barcodeDataList: ArrayList<BarcodeData>? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Set up the UI
//        setContentView(R.layout.activity_barcode_search)
//        videoView = findViewById(R.id.videoView)
//        barcodeInput = findViewById(R.id.barcodeInput)
//        searchButton = findViewById(R.id.searchButton)
//
//        // Retrieve data from intent
//        val videoUriString = intent.getStringExtra("videoUri")
//        videoUri = if (videoUriString != null) Uri.parse(videoUriString) else null
//        barcodeDataList = intent.getSerializableExtra("barcodeDataList") as? ArrayList<BarcodeData>
//
//        searchButton.setOnClickListener {
//            val inputBarcode = barcodeInput.text.toString()
//            searchBarcode(inputBarcode)
//        }
//
//        // Set an error listener for better debugging
//        videoView.setOnErrorListener { _, what, extra ->
//            Log.e("BarcodeSearchActivity", "VideoView Error: what=$what, extra=$extra")
//            Toast.makeText(this, "Error playing video, please try again", Toast.LENGTH_SHORT).show()
//            true
//        }
//    }
//
//    private fun searchBarcode(barcodeValue: String) {
//        val matchingBarcode = barcodeDataList?.find { it.value == barcodeValue }
//        if (matchingBarcode != null && videoUri != null) {
//            playVideoAtTimestamp(videoUri!!, matchingBarcode.timestamp)
//        } else {
//            Toast.makeText(this, "Barcode not found", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun playVideoAtTimestamp(videoUri: Uri, timestamp: Long) {
//        // Set up the VideoView with the video Uri
//        videoView.setVideoURI(videoUri)
//
//        // Create and attach MediaController for built-in playback controls
//        val mediaController = MediaController(this)
//        videoView.setMediaController(mediaController)
//        mediaController.setAnchorView(videoView)  // Attach the controller to the VideoView
//
//        // Add a listener to handle video playback once the video is prepared
//        videoView.setOnPreparedListener { mediaPlayer ->
//            try {
//                mediaPlayer.isLooping = false  // Ensure video only plays once
//                mediaPlayer.seekTo(timestamp.toInt())
//                videoView.pause()  // Pause initially so that user has to press play manually
//
//                // Inform the user that the video is ready to be played
//                Toast.makeText(
//                    this,
//                    "Video is ready at the barcode. Tap the play",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//            } catch (e: Exception) {
//                Log.e("BarcodeSearchActivity", "Error while seeking: ${e.message}")
//                Toast.makeText(this, "Error playing video, please try again", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Set an error listener to handle any issues during video playback
//        videoView.setOnErrorListener { _, what, extra ->
//            Log.e("BarcodeSearchActivity", "MediaPlayer Error: what=$what, extra=$extra")
//            Toast.makeText(this, "Error playing video, please try again", Toast.LENGTH_SHORT).show()
//            true
//        }
//    }
//}
//


package com.google.mlkit.md

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.MediaController

class BarcodeSearchActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var barcodeInput: EditText
    private lateinit var searchButton: Button
    private lateinit var reRecordButton: Button

    private var videoUri: Uri? = null
    private var barcodeDataList: ArrayList<BarcodeData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the UI
        setContentView(R.layout.activity_barcode_search)
        videoView = findViewById(R.id.videoView)
        barcodeInput = findViewById(R.id.barcodeInput)
        searchButton = findViewById(R.id.searchButton)
        reRecordButton = findViewById(R.id.reRecordButton)

        // Retrieve data from intent
        val videoUriString = intent.getStringExtra("videoUri")
        videoUri = if (videoUriString != null) Uri.parse(videoUriString) else null
        barcodeDataList = intent.getSerializableExtra("barcodeDataList") as? ArrayList<BarcodeData>

        searchButton.setOnClickListener {
            val inputBarcode = barcodeInput.text.toString()
            searchBarcode(inputBarcode)
        }

        // Handle Re-record button click to go back to LiveBarcodeScanningActivity
        reRecordButton.setOnClickListener {
            navigateToLiveBarcodeScanningActivity()
        }

        // Set an error listener for better debugging
        videoView.setOnErrorListener { _, what, extra ->
            Log.e("BarcodeSearchActivity", "VideoView Error: what=$what, extra=$extra")
            Toast.makeText(this, "Error playing video, please try again", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun searchBarcode(barcodeValue: String) {
        val matchingBarcode = barcodeDataList?.find { it.value == barcodeValue }
        if (matchingBarcode != null && videoUri != null) {
            playVideoAtTimestamp(videoUri!!, matchingBarcode.timestamp)
        } else {
            Toast.makeText(this, "Barcode not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playVideoAtTimestamp(videoUri: Uri, timestamp: Long) {
        // Set up the VideoView with the video Uri
        videoView.setVideoURI(videoUri)

        // Create and attach MediaController for built-in playback controls
        val mediaController = MediaController(this)
        videoView.setMediaController(mediaController)
        mediaController.setAnchorView(videoView)  // Attach the controller to the VideoView

        // Add a listener to handle video playback once the video is prepared
        videoView.setOnPreparedListener { mediaPlayer ->
            try {
                mediaPlayer.isLooping = false  // Ensure video only plays once
                mediaPlayer.seekTo(timestamp.toInt())
                videoView.pause()  // Pause initially so that user has to press play manually

                // Inform the user that the video is ready to be played
                Toast.makeText(
                    this,
                    "Video is ready at the barcode. Tap the play button to start.",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e("BarcodeSearchActivity", "Error while seeking: ${e.message}")
                Toast.makeText(this, "Error playing video, please try again", Toast.LENGTH_SHORT).show()
            }
        }

        // Set an error listener to handle any issues during video playback
        videoView.setOnErrorListener { _, what, extra ->
            Log.e("BarcodeSearchActivity", "MediaPlayer Error: what=$what, extra=$extra")
            Toast.makeText(this, "Error playing video, please try again", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun navigateToLiveBarcodeScanningActivity() {
        val intent = Intent(this, LiveBarcodeScanningActivity::class.java)
        // Optional: Add any flags if needed, like FLAG_ACTIVITY_CLEAR_TOP to remove this activity from stack
        startActivity(intent)
        finish()  // Finish this activity so that it gets removed from the stack
    }
}
