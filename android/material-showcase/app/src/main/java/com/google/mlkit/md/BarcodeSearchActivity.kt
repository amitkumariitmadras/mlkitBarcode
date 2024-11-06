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


package com.google.mlkit.md

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

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
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.seekTo(timestamp.toInt())
            videoView.start()
        }
    }
}
