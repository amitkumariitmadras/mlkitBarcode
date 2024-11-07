//package com.google.mlkit.md
//
//import android.media.MediaPlayer
//import android.net.Uri
//import android.os.Bundle
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//
//class BarcodeSearchActivity : AppCompatActivity() {
//
//    private lateinit var videoView: VideoView
//    private lateinit var barcodeInput: EditText
//    private lateinit var searchButton: Button
//
//    private var videoPath: String? = null
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
//        videoPath = intent.getStringExtra("videoPath")
//        barcodeDataList = intent.getSerializableExtra("barcodeDataList") as? ArrayList<BarcodeData>
//
//        searchButton.setOnClickListener {
//            val inputBarcode = barcodeInput.text.toString()
//            searchBarcode(inputBarcode)
//        }
//    }
//
//    private fun searchBarcode(barcodeValue: String) {
//        val matchingBarcode = barcodeDataList?.find { it.value == barcodeValue }
//        if (matchingBarcode != null && videoPath != null) {
//            playVideoAtTimestamp(videoPath!!, matchingBarcode.timestamp)
//        } else {
//            Toast.makeText(this, "Barcode not found", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun playVideoAtTimestamp(videoPath: String, timestamp: Long) {
//        val uri = Uri.parse(videoPath)
//        videoView.setVideoURI(uri)
//        videoView.setOnPreparedListener { mediaPlayer ->
//            mediaPlayer.seekTo(timestamp.toInt())
//            videoView.start()
//        }
//    }
//}

//
//package com.google.mlkit.md
//
//import android.net.Uri
//import android.os.Bundle
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
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
//        videoView.setVideoURI(videoUri)
//        videoView.setOnPreparedListener { mediaPlayer ->
//            mediaPlayer.seekTo(timestamp.toInt())
//            videoView.start()
//        }
//    }
//}

//
//package com.google.mlkit.md
//
//import android.media.MediaPlayer
//import android.net.Uri
//import android.os.Bundle
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
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
//        setContentView(R.layout.activity_barcode_search)
//
//        // Initialize Views
//        videoView = findViewById(R.id.videoView)
//        barcodeInput = findViewById(R.id.barcodeInput)
//        searchButton = findViewById(R.id.searchButton)
//
//        // Retrieve data from intent
//        val videoUriString = intent.getStringExtra("videoUri")
//        videoUri = if (videoUriString != null) Uri.parse(videoUriString) else null
//        barcodeDataList = intent.getSerializableExtra("barcodeDataList") as? ArrayList<BarcodeData>
//
//        // Add media controller for VideoView
//        videoView.setMediaController(MediaController(this))
//
//        // Set up search button click listener
//        searchButton.setOnClickListener {
//            val inputBarcode = barcodeInput.text.toString()
//            searchBarcode(inputBarcode)
//        }
//    }
//
//    private fun searchBarcode(barcodeValue: String) {
//        val matchingBarcode = barcodeDataList?.find { it.value == barcodeValue }
//        if (matchingBarcode != null && videoUri != null) {
//            stopAndPrepareVideo(videoUri!!, matchingBarcode.timestamp)
//        } else {
//            Toast.makeText(this, "Barcode not found", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun stopAndPrepareVideo(videoUri: Uri, timestamp: Long) {
//        // Set the URI to the VideoView
//        videoView.setVideoURI(videoUri)
//
//        // Set a listener to prepare the video and seek to the desired position
//        videoView.setOnPreparedListener { mediaPlayer ->
//            val duration = mediaPlayer.duration
//
//            // Ensure the timestamp is within the video duration
//            val seekPosition = if (timestamp <= duration) timestamp.toInt() else 0
//
//            // Seek to the calculated position
//            mediaPlayer.seekTo(seekPosition)
//
//            // Stop playback until user presses play
//            mediaPlayer.pause()
//        }
//
//        // Optional: Handle completion if any actions are required
//        videoView.setOnCompletionListener {
//            Toast.makeText(this, "Video playback completed", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
//
//package com.google.mlkit.md
//
//import android.net.Uri
//import android.os.Bundle
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import android.media.MediaPlayer
//import android.util.Log
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
//        // Add a listener to handle video playback once the video is prepared
//        videoView.setOnPreparedListener { mediaPlayer ->
//            try {
//                mediaPlayer.isLooping = false  // Ensure video only plays once
//                mediaPlayer.seekTo(timestamp.toInt())
//                // Stop video by default at the timestamp until user presses play
//                videoView.pause()
//                // Set a play button or let the user tap on the VideoView to play from the timestamp
//                Toast.makeText(this, "Video is ready at the barcode timestamp. Tap to play.", Toast.LENGTH_SHORT).show()
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

    private var videoUri: Uri? = null
    private var barcodeDataList: ArrayList<BarcodeData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up the UI
        setContentView(R.layout.activity_barcode_search)
        videoView = findViewById(R.id.videoView)
        barcodeInput = findViewById(R.id.barcodeInput)
        searchButton = findViewById(R.id.searchButton)

        // Retrieve data from intent
        val videoUriString = intent.getStringExtra("videoUri")
        videoUri = if (videoUriString != null) Uri.parse(videoUriString) else null
        barcodeDataList = intent.getSerializableExtra("barcodeDataList") as? ArrayList<BarcodeData>

        searchButton.setOnClickListener {
            val inputBarcode = barcodeInput.text.toString()
            searchBarcode(inputBarcode)
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
                    "Video is ready at the barcode. Tap the play",
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
}

