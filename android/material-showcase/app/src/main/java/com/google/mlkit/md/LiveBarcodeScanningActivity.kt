///*
// * Copyright 2020 Google LLC
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.google.mlkit.md
//
//import android.animation.AnimatorInflater
//import android.animation.AnimatorSet
//import android.content.Intent
//import android.hardware.Camera
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.view.View.OnClickListener
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProviders
//import com.google.android.material.chip.Chip
//import com.google.common.base.Objects
//import com.google.mlkit.md.camera.GraphicOverlay
//import com.google.mlkit.md.camera.WorkflowModel
//import com.google.mlkit.md.camera.WorkflowModel.WorkflowState
//import com.google.mlkit.md.barcodedetection.BarcodeField
//import com.google.mlkit.md.barcodedetection.BarcodeProcessor
//import com.google.mlkit.md.barcodedetection.BarcodeResultFragment
//import com.google.mlkit.md.camera.CameraSource
//import com.google.mlkit.md.camera.CameraSourcePreview
//import com.google.mlkit.md.settings.SettingsActivity
//import java.io.IOException
//import java.util.ArrayList
//
///** Demonstrates the barcode scanning workflow using camera preview.  */
//class LiveBarcodeScanningActivity : AppCompatActivity(), OnClickListener {
//
//    private var cameraSource: CameraSource? = null
//    private var preview: CameraSourcePreview? = null
//    private var graphicOverlay: GraphicOverlay? = null
//    private var settingsButton: View? = null
//    private var flashButton: View? = null
//    private var promptChip: Chip? = null
//    private var promptChipAnimator: AnimatorSet? = null
//    private var workflowModel: WorkflowModel? = null
//    private var currentWorkflowState: WorkflowState? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.activity_live_barcode)
//        preview = findViewById(R.id.camera_preview)
//        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//            cameraSource = CameraSource(this)
//        }
//
//        promptChip = findViewById(R.id.bottom_prompt_chip)
//        promptChipAnimator =
//            (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
//                setTarget(promptChip)
//            }
//
//        findViewById<View>(R.id.close_button).setOnClickListener(this)
//        flashButton = findViewById<View>(R.id.flash_button).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//        }
//        settingsButton = findViewById<View>(R.id.settings_button).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//        }
//
//        setUpWorkflowModel()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        workflowModel?.markCameraFrozen()
//        settingsButton?.isEnabled = true
//        currentWorkflowState = WorkflowState.NOT_STARTED
//        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!))
//        workflowModel?.setWorkflowState(WorkflowState.DETECTING)
//    }
//
//    override fun onPostResume() {
//        super.onPostResume()
//        BarcodeResultFragment.dismiss(supportFragmentManager)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        currentWorkflowState = WorkflowState.NOT_STARTED
//        stopCameraPreview()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraSource?.release()
//        cameraSource = null
//    }
//
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.close_button -> onBackPressed()
//            R.id.flash_button -> {
//                flashButton?.let {
//                    if (it.isSelected) {
//                        it.isSelected = false
//                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
//                    } else {
//                        it.isSelected = true
//                        cameraSource!!.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
//                    }
//                }
//            }
//            R.id.settings_button -> {
//                settingsButton?.isEnabled = false
//                startActivity(Intent(this, SettingsActivity::class.java))
//            }
//        }
//    }
//
//    private fun startCameraPreview() {
//        val workflowModel = this.workflowModel ?: return
//        val cameraSource = this.cameraSource ?: return
//        if (!workflowModel.isCameraLive) {
//            try {
//                workflowModel.markCameraLive()
//                preview?.start(cameraSource)
//            } catch (e: IOException) {
//                Log.e(TAG, "Failed to start camera preview!", e)
//                cameraSource.release()
//                this.cameraSource = null
//            }
//        }
//    }
//
//    private fun stopCameraPreview() {
//        val workflowModel = this.workflowModel ?: return
//        if (workflowModel.isCameraLive) {
//            workflowModel.markCameraFrozen()
//            flashButton?.isSelected = false
//            preview?.stop()
//        }
//    }
//
//    private fun setUpWorkflowModel() {
//        workflowModel = ViewModelProviders.of(this).get(WorkflowModel::class.java)
//
//        // Observes the workflow state changes, if happens, update the overlay view indicators and
//        // camera preview state.
//        workflowModel!!.workflowState.observe(this, Observer { workflowState ->
//            if (workflowState == null || Objects.equal(currentWorkflowState, workflowState)) {
//                return@Observer
//            }
//
//            currentWorkflowState = workflowState
//            Log.d(TAG, "Current workflow state: ${currentWorkflowState!!.name}")
//
//            val wasPromptChipGone = promptChip?.visibility == View.GONE
//
//            when (workflowState) {
//                WorkflowState.DETECTING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
//                    startCameraPreview()
//                }
//                WorkflowState.CONFIRMING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_move_camera_closer)
//                    startCameraPreview()
//                }
//                WorkflowState.SEARCHING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_searching)
//                    stopCameraPreview()
//                }
//                WorkflowState.DETECTED, WorkflowState.SEARCHED -> {
//                    promptChip?.visibility = View.GONE
//                    stopCameraPreview()
//                }
//                else -> promptChip?.visibility = View.GONE
//            }
//
//            val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip?.visibility == View.VISIBLE
//            promptChipAnimator?.let {
//                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
//            }
//        })
//
//        workflowModel?.detectedBarcode?.observe(this, Observer { barcode ->
//            if (barcode != null) {
//                val barcodeFieldList = ArrayList<BarcodeField>()
//                barcodeFieldList.add(BarcodeField("Raw Value", barcode.rawValue ?: ""))
//                BarcodeResultFragment.show(supportFragmentManager, barcodeFieldList)
//            }
//        })
//    }
//
//    companion object {
//        private const val TAG = "LiveBarcodeActivity"
//    }
//}

//
//package com.google.mlkit.md
//
//import android.animation.AnimatorInflater
//import android.animation.AnimatorSet
//import android.content.Intent
//import android.hardware.Camera
//import android.media.MediaRecorder
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.view.View.OnClickListener
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import com.google.android.material.chip.Chip
//import com.google.mlkit.md.barcodedetection.BarcodeField
//import com.google.mlkit.md.barcodedetection.BarcodeProcessor
//import com.google.mlkit.md.barcodedetection.BarcodeResultFragment
//import com.google.mlkit.md.camera.CameraSource
//import com.google.mlkit.md.camera.CameraSourcePreview
//import com.google.mlkit.md.camera.GraphicOverlay
//import com.google.mlkit.md.camera.WorkflowModel
//import com.google.mlkit.md.camera.WorkflowModel.WorkflowState
//import com.google.mlkit.md.settings.SettingsActivity
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.*
//
///** Demonstrates the barcode scanning workflow using camera preview and records video. */
//class LiveBarcodeScanningActivity : AppCompatActivity(), OnClickListener {
//
//    private var cameraSource: CameraSource? = null
//    private var preview: CameraSourcePreview? = null
//    private var graphicOverlay: GraphicOverlay? = null
//    private var settingsButton: View? = null
//    private var flashButton: View? = null
//    private var promptChip: Chip? = null
//    private var promptChipAnimator: AnimatorSet? = null
//    private var workflowModel: WorkflowModel? = null
//    private var currentWorkflowState: WorkflowState? = null
//
//    private var mediaRecorder: MediaRecorder? = null
//    private var isRecording = false
//    private var videoFile: File? = null
//    private var recordingStartTime: Long = 0L
//    private val detectedBarcodes = mutableListOf<BarcodeData>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.activity_live_barcode)
//        preview = findViewById(R.id.camera_preview)
//        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//            cameraSource = CameraSource(this)
//        }
//
//        promptChip = findViewById(R.id.bottom_prompt_chip)
//        promptChipAnimator =
//            (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
//                setTarget(promptChip)
//            }
//
//        findViewById<View>(R.id.close_button).setOnClickListener(this)
//        flashButton = findViewById<View>(R.id.flash_button).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//        }
//        settingsButton = findViewById<View>(R.id.settings_button).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//        }
//
//        setUpWorkflowModel()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        workflowModel?.markCameraFrozen()
//        settingsButton?.isEnabled = true
//        currentWorkflowState = WorkflowState.NOT_STARTED
//        cameraSource?.setFrameProcessor(BarcodeProcessor(graphicOverlay!!, workflowModel!!, ::handleDetectedBarcode))
//        workflowModel?.setWorkflowState(WorkflowState.DETECTING)
//    }
//
//    override fun onPostResume() {
//        super.onPostResume()
//        BarcodeResultFragment.dismiss(supportFragmentManager)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        currentWorkflowState = WorkflowState.NOT_STARTED
//        stopCameraPreview()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopRecording()
//        cameraSource?.release()
//        cameraSource = null
//    }
//
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.close_button -> onBackPressed()
//            R.id.flash_button -> {
//                flashButton?.let {
//                    if (it.isSelected) {
//                        it.isSelected = false
//                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
//                    } else {
//                        it.isSelected = true
//                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
//                    }
//                }
//            }
//            R.id.settings_button -> {
//                settingsButton?.isEnabled = false
//                startActivity(Intent(this, SettingsActivity::class.java))
//            }
//        }
//    }
//
//    private fun startCameraPreview() {
//        val workflowModel = this.workflowModel ?: return
//        val cameraSource = this.cameraSource ?: return
//        if (!workflowModel.isCameraLive) {
//            try {
//                workflowModel.markCameraLive()
//                preview?.start(cameraSource)
//                startRecording()
//            } catch (e: IOException) {
//                Log.e(TAG, "Failed to start camera preview!", e)
//                cameraSource.release()
//                this.cameraSource = null
//            }
//        }
//    }
//
//    private fun stopCameraPreview() {
//        val workflowModel = this.workflowModel ?: return
//        if (workflowModel.isCameraLive) {
//            workflowModel.markCameraFrozen()
//            flashButton?.isSelected = false
//            preview?.stop()
//            stopRecording()
//        }
//    }
//
//    private fun setUpWorkflowModel() {
//        workflowModel = ViewModelProvider(this).get(WorkflowModel::class.java)
//
//        // Observes the workflow state changes, if happens, update the overlay view indicators and
//        // camera preview state.
//        workflowModel!!.workflowState.observe(this, Observer { workflowState ->
//            if (workflowState == null || workflowState == currentWorkflowState) {
//                return@Observer
//            }
//
//            currentWorkflowState = workflowState
//            Log.d(TAG, "Current workflow state: ${currentWorkflowState!!.name}")
//
//            val wasPromptChipGone = promptChip?.visibility == View.GONE
//
//            when (workflowState) {
//                WorkflowState.DETECTING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
//                    startCameraPreview()
//                }
//                WorkflowState.CONFIRMING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_move_camera_closer)
//                    startCameraPreview()
//                }
//                WorkflowState.SEARCHING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_searching)
//                    stopCameraPreview()
//                }
//                WorkflowState.DETECTED, WorkflowState.SEARCHED -> {
//                    promptChip?.visibility = View.GONE
//                    stopCameraPreview()
//                    // After detection is done, navigate to search activity
//                    navigateToSearchActivity()
//                }
//                else -> promptChip?.visibility = View.GONE
//            }
//
//            val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip?.visibility == View.VISIBLE
//            promptChipAnimator?.let {
//                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
//            }
//        })
//
//        workflowModel?.detectedBarcode?.observe(this, Observer { barcode ->
//            if (barcode != null) {
//                val barcodeFieldList = ArrayList<BarcodeField>()
//                barcodeFieldList.add(BarcodeField("Raw Value", barcode.rawValue ?: ""))
//                BarcodeResultFragment.show(supportFragmentManager, barcodeFieldList)
//                // Handle detected barcode
//                handleDetectedBarcode(barcode.rawValue ?: "")
//            }
//        })
//    }
//
//    private fun startRecording() {
//        if (isRecording) return
//
//        mediaRecorder = MediaRecorder().apply {
//            // Configure MediaRecorder settings
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setVideoSource(MediaRecorder.VideoSource.SURFACE)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            val videoFilePath = getVideoFilePath()
//            videoFile = File(videoFilePath)
//            setOutputFile(videoFile?.absolutePath)
//            setVideoEncodingBitRate(10000000)
//            setVideoFrameRate(30)
//            setVideoSize(cameraSource?.previewSize?.width ?: 1920, cameraSource?.previewSize?.height ?: 1080)
//            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            try {
//                prepare()
//                start()
//                isRecording = true
//                recordingStartTime = System.currentTimeMillis()
//            } catch (e: IOException) {
//                Log.e(TAG, "Failed to start recording", e)
//            }
//        }
//    }
//
//    private fun stopRecording() {
//        if (!isRecording) return
//
//        mediaRecorder?.apply {
//            try {
//                stop()
//                release()
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to stop recording", e)
//            }
//            isRecording = false
//        }
//        mediaRecorder = null
//    }
//
//    private fun getVideoFilePath(): String {
//        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
//        val videoFileName = "BarcodeVideo_$timestamp.mp4"
//        val videoDir = getExternalFilesDir(null)
//        return File(videoDir, videoFileName).absolutePath
//    }
//
//    private fun handleDetectedBarcode(barcodeValue: String) {
//        val currentTime = System.currentTimeMillis()
//        val timestamp = currentTime - recordingStartTime
//
//        val barcodeData = BarcodeData(
//            value = barcodeValue,
//            timestamp = timestamp
//        )
//        detectedBarcodes.add(barcodeData)
//
//        // Optional: Provide feedback to the user
//        runOnUiThread {
//            // You can update UI here if needed
//        }
//    }
//
//    private fun navigateToSearchActivity() {
//        val intent = Intent(this, BarcodeSearchActivity::class.java)
//        intent.putExtra("videoPath", videoFile?.absolutePath)
//        intent.putExtra("barcodeDataList", ArrayList(detectedBarcodes))
//        startActivity(intent)
//        finish()
//    }
//
//    companion object {
//        private const val TAG = "LiveBarcodeActivity"
//    }
//}

//
//package com.google.mlkit.md
//
//import android.animation.AnimatorInflater
//import android.animation.AnimatorSet
//import android.content.Intent
//import android.hardware.Camera
//import android.media.MediaRecorder
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.view.View.OnClickListener
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import com.google.android.material.chip.Chip
//import com.google.mlkit.md.barcodedetection.BarcodeField
//import com.google.mlkit.md.barcodedetection.BarcodeProcessor
//import com.google.mlkit.md.barcodedetection.BarcodeResultFragment
//import com.google.mlkit.md.camera.CameraSource
//import com.google.mlkit.md.camera.CameraSourcePreview
//import com.google.mlkit.md.camera.GraphicOverlay
//import com.google.mlkit.md.camera.WorkflowModel
//import com.google.mlkit.md.camera.WorkflowModel.WorkflowState
//import com.google.mlkit.md.settings.SettingsActivity
//import com.google.mlkit.vision.barcode.common.Barcode
//import java.io.File
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.*
//
///** Demonstrates the barcode scanning workflow using camera preview and records video. */
//class LiveBarcodeScanningActivity : AppCompatActivity(), OnClickListener {
//
//    private var cameraSource: CameraSource? = null
//    private var preview: CameraSourcePreview? = null
//    private var graphicOverlay: GraphicOverlay? = null
//    private var settingsButton: View? = null
//    private var flashButton: View? = null
//    private var promptChip: Chip? = null
//    private var promptChipAnimator: AnimatorSet? = null
//    private var workflowModel: WorkflowModel? = null
//    private var currentWorkflowState: WorkflowState? = null
//
//    private var mediaRecorder: MediaRecorder? = null
//    private var isRecording = false
//    private var videoFile: File? = null
//    private var recordingStartTime: Long = 0L
//    private val detectedBarcodes = mutableListOf<BarcodeData>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.activity_live_barcode)
//        preview = findViewById(R.id.camera_preview)
//        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//            cameraSource = CameraSource(this)
//        }
//
//        promptChip = findViewById(R.id.bottom_prompt_chip)
//        promptChipAnimator =
//            (AnimatorInflater.loadAnimator(this, R.animator.bottom_prompt_chip_enter) as AnimatorSet).apply {
//                setTarget(promptChip)
//            }
//
//        findViewById<View>(R.id.close_button).setOnClickListener(this)
//        flashButton = findViewById<View>(R.id.flash_button).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//        }
//        settingsButton = findViewById<View>(R.id.settings_button).apply {
//            setOnClickListener(this@LiveBarcodeScanningActivity)
//        }
//
//        setUpWorkflowModel()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        workflowModel?.markCameraFrozen()
//        settingsButton?.isEnabled = true
//        currentWorkflowState = WorkflowState.NOT_STARTED
//        cameraSource?.setFrameProcessor(
//            BarcodeProcessor(
//                graphicOverlay = graphicOverlay!!,
//                workflowModel = workflowModel!!,
//                onBarcodeDetected = ::handleDetectedBarcode // Corrected function reference
//            )
//        )
//        workflowModel?.setWorkflowState(WorkflowState.DETECTING)
//    }
//
//    override fun onPostResume() {
//        super.onPostResume()
//        BarcodeResultFragment.dismiss(supportFragmentManager)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        currentWorkflowState = WorkflowState.NOT_STARTED
//        stopCameraPreview()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopRecording()
//        cameraSource?.release()
//        cameraSource = null
//    }
//
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.close_button -> onBackPressed()
//            R.id.flash_button -> {
//                flashButton?.let {
//                    if (it.isSelected) {
//                        it.isSelected = false
//                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_OFF)
//                    } else {
//                        it.isSelected = true
//                        cameraSource?.updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
//                    }
//                }
//            }
//            R.id.settings_button -> {
//                settingsButton?.isEnabled = false
//                startActivity(Intent(this, SettingsActivity::class.java))
//            }
//        }
//    }
//
//    private fun startCameraPreview() {
//        val workflowModel = this.workflowModel ?: return
//        val cameraSource = this.cameraSource ?: return
//        if (!workflowModel.isCameraLive) {
//            try {
//                workflowModel.markCameraLive()
//                preview?.start(cameraSource)
//                startRecording()
//            } catch (e: IOException) {
//                Log.e(TAG, "Failed to start camera preview!", e)
//                cameraSource.release()
//                this.cameraSource = null
//            }
//        }
//    }
//
//    private fun stopCameraPreview() {
//        val workflowModel = this.workflowModel ?: return
//        if (workflowModel.isCameraLive) {
//            workflowModel.markCameraFrozen()
//            flashButton?.isSelected = false
//            preview?.stop()
//            stopRecording()
//        }
//    }
//
//    private fun setUpWorkflowModel() {
//        workflowModel = ViewModelProvider(this).get(WorkflowModel::class.java)
//
//        // Observes the workflow state changes, if happens, update the overlay view indicators and
//        // camera preview state.
//        workflowModel!!.workflowState.observe(this, Observer { workflowState ->
//            if (workflowState == null || workflowState == currentWorkflowState) {
//                return@Observer
//            }
//
//            currentWorkflowState = workflowState
//            Log.d(TAG, "Current workflow state: ${currentWorkflowState!!.name}")
//
//            val wasPromptChipGone = promptChip?.visibility == View.GONE
//
//            when (workflowState) {
//                WorkflowState.DETECTING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_point_at_a_barcode)
//                    startCameraPreview()
//                }
//                WorkflowState.CONFIRMING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_move_camera_closer)
//                    startCameraPreview()
//                }
//                WorkflowState.SEARCHING -> {
//                    promptChip?.visibility = View.VISIBLE
//                    promptChip?.setText(R.string.prompt_searching)
//                    stopCameraPreview()
//                }
//                WorkflowState.DETECTED, WorkflowState.SEARCHED -> {
//                    promptChip?.visibility = View.GONE
//                    stopCameraPreview()
//                    // After detection is done, navigate to search activity
//                    navigateToSearchActivity()
//                }
//                else -> promptChip?.visibility = View.GONE
//            }
//
//            val shouldPlayPromptChipEnteringAnimation = wasPromptChipGone && promptChip?.visibility == View.VISIBLE
//            promptChipAnimator?.let {
//                if (shouldPlayPromptChipEnteringAnimation && !it.isRunning) it.start()
//            }
//        })
//
//        workflowModel?.detectedBarcode?.observe(this, Observer { barcode ->
//            if (barcode != null) {
//                val barcodeFieldList = ArrayList<BarcodeField>()
//                barcodeFieldList.add(BarcodeField("Raw Value", barcode.rawValue ?: ""))
//                BarcodeResultFragment.show(supportFragmentManager, barcodeFieldList)
//                // Handle detected barcode
//                handleDetectedBarcode(barcode)
//            }
//        })
//    }
//
//    private fun startRecording() {
//        if (isRecording) return
//
//        mediaRecorder = MediaRecorder().apply {
//            // Configure MediaRecorder settings
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setVideoSource(MediaRecorder.VideoSource.SURFACE)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            val videoFilePath = getVideoFilePath()
//            videoFile = File(videoFilePath)
//            setOutputFile(videoFile?.absolutePath)
//            setVideoEncodingBitRate(10000000)
//            setVideoFrameRate(30)
//            setVideoSize(cameraSource?.previewSize?.width ?: 1920, cameraSource?.previewSize?.height ?: 1080)
//            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            try {
//                prepare()
//                start()
//                isRecording = true
//                recordingStartTime = System.currentTimeMillis()
//            } catch (e: IOException) {
//                Log.e(TAG, "Failed to start recording", e)
//            }
//        }
//    }
//
//    private fun stopRecording() {
//        if (!isRecording) return
//
//        mediaRecorder?.apply {
//            try {
//                stop()
//                release()
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to stop recording", e)
//            }
//            isRecording = false
//        }
//        mediaRecorder = null
//    }
//
//    private fun getVideoFilePath(): String {
//        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
//        val videoFileName = "BarcodeVideo_$timestamp.mp4"
//        val videoDir = getExternalFilesDir(null)
//        return File(videoDir, videoFileName).absolutePath
//    }
//
//    private fun handleDetectedBarcode(barcode: Barcode) {
//        val barcodeValue = barcode.rawValue ?: return
//        val currentTime = System.currentTimeMillis()
//        val timestamp = currentTime - recordingStartTime
//
//        val barcodeData = BarcodeData(
//            value = barcodeValue,
//            timestamp = timestamp
//        )
//        detectedBarcodes.add(barcodeData)
//
//        // Optional: Provide feedback to the user
//        runOnUiThread {
//            Toast.makeText(this, "Barcode detected: $barcodeValue", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun navigateToSearchActivity() {
//        val intent = Intent(this, BarcodeSearchActivity::class.java)
//        intent.putExtra("videoPath", videoFile?.absolutePath)
//        intent.putExtra("barcodeDataList", ArrayList(detectedBarcodes))
//        startActivity(intent)
//        finish()
//    }
//
//    companion object {
//        private const val TAG = "LiveBarcodeActivity"
//    }
//}

//
//package com.google.mlkit.md
//
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Intent
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.view.View.OnClickListener
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.*
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.mlkit.md.barcodedetection.BarcodeField
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.common.InputImage
//import java.text.SimpleDateFormat
//import java.util.*
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class LiveBarcodeScanningActivity : AppCompatActivity(), OnClickListener {
//
//    private var preview: Preview? = null
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var imageAnalysis: ImageAnalysis? = null
//    private lateinit var cameraExecutor: ExecutorService
//    private var recording: Recording? = null
//    private var recordingStartTime: Long = 0L
//    private var videoFileUri: Uri? = null
//    private val detectedBarcodes = mutableListOf<BarcodeData>()
//
//
//
//    private val REQUIRED_PERMISSIONS = arrayOf(
//        android.Manifest.permission.CAMERA,
//        android.Manifest.permission.RECORD_AUDIO
//    )
//    private val REQUEST_CODE_PERMISSIONS = 10
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        setContentView(R.layout.activity_live_barcode)
//
//        // Request permissions
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            // Used to bind the lifecycle of cameras to the lifecycle owner
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Preview
//            preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
//                }
//
//            // VideoCapture
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            // ImageAnalysis for barcode scanning
//            imageAnalysis = ImageAnalysis.Builder()
//                .build()
//                .also {
//                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
//                }
//
//            // Select back camera
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll()
//
//                // Bind use cases to camera
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, videoCapture, imageAnalysis
//                )
//
//                // Start video recording
//                startRecording()
//
//            } catch (exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startRecording() {
//        val videoCapture = this.videoCapture ?: return
//
//        val name = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, "BarcodeVideo_$name")
//            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/BarcodeVideos")
//            }
//        }
//
//        val outputOptions = MediaStoreOutputOptions.Builder(
//            contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        ).setContentValues(contentValues).build()
//
//        recording = videoCapture.output.prepareRecording(this, outputOptions)
//            .withAudioEnabled()
//            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
//                when (recordEvent) {
//                    is VideoRecordEvent.Start -> {
//                        recordingStartTime = System.currentTimeMillis()
//                        Log.d(TAG, "Recording started")
//                    }
//                    is VideoRecordEvent.Finalize -> {
//                        if (!recordEvent.hasError()) {
//                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
//                            Log.d(TAG, msg)
//                            videoFileUri = recordEvent.outputResults.outputUri
//                            navigateToSearchActivity()
//                        } else {
//                            recording?.close()
//                            recording = null
//                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
//                        }
//                    }
//                }
//            }
//    }
//
//    private fun stopRecording() {
//        recording?.stop()
//        recording = null
//    }
//
//    private fun handleDetectedBarcode(barcode: Barcode) {
//        val barcodeValue = barcode.rawValue ?: return
//        val currentTime = System.currentTimeMillis()
//        val timestamp = currentTime - recordingStartTime
//
//        val barcodeData = BarcodeData(
//            value = barcodeValue,
//            timestamp = timestamp
//        )
//        detectedBarcodes.add(barcodeData)
//
//        // Optional: Provide feedback to the user
//        runOnUiThread {
//            Toast.makeText(this, "Barcode detected: $barcodeValue", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
//        private val scanner = BarcodeScanning.getClient()
//
//        @androidx.camera.core.ExperimentalGetImage
//        override fun analyze(imageProxy: ImageProxy) {
//            @androidx.camera.core.ExperimentalGetImage
//            val mediaImage = imageProxy.image
//            if (mediaImage != null) {
//                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//                scanner.process(image)
//                    .addOnSuccessListener { barcodes ->
//                        for (barcode in barcodes) {
//                            handleDetectedBarcode(barcode)
//                        }
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e(TAG, "Barcode analysis error", e)
//                    }
//                    .addOnCompleteListener {
//                        imageProxy.close()
//                    }
//            } else {
//                imageProxy.close()
//            }
//        }
//    }
//
//    private fun navigateToSearchActivity() {
//        val intent = Intent(this, BarcodeSearchActivity::class.java)
//        intent.putExtra("videoUri", videoFileUri.toString())
//        intent.putExtra("barcodeDataList", ArrayList(detectedBarcodes))
//        startActivity(intent)
//        finish()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopRecording()
//        cameraExecutor.shutdown()
//    }
//
//    override fun onClick(v: View?) {
//        // Handle button clicks if needed
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(
//            baseContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera()
//            } else {
//                Toast.makeText(this,
//                    "Permissions not granted by the user.",
//                    Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "LiveBarcodeActivity"
//    }
//}

//
//package com.google.mlkit.md
//
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Intent
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.*
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.mlkit.md.barcodedetection.BarcodeField
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.common.InputImage
//import java.text.SimpleDateFormat
//import java.util.*
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class LiveBarcodeScanningActivity : AppCompatActivity(), View.OnClickListener {
//
//    private var preview: Preview? = null
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var imageAnalysis: ImageAnalysis? = null
//    private lateinit var cameraExecutor: ExecutorService
//    private var recording: Recording? = null
//    private var recordingStartTime: Long = 0L
//    private var videoFileUri: Uri? = null
//    private val detectedBarcodes = mutableListOf<BarcodeData>()
//
//    private val REQUIRED_PERMISSIONS = arrayOf(
//        android.Manifest.permission.CAMERA,
//        android.Manifest.permission.RECORD_AUDIO
//    )
//    private val REQUEST_CODE_PERMISSIONS = 10
//
//    private lateinit var recordButton: FloatingActionButton
//    private lateinit var recordingDuration: TextView
//    private var isRecording = false
//    private val handler = Handler()
//    private lateinit var updateDurationRunnable: Runnable
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_live_barcode)
//
//        // Request permissions
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//            )
//        }
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        // Initialize record button and recording duration TextView
//        recordButton = findViewById(R.id.record_button)
//        recordingDuration = findViewById(R.id.recording_duration)
//
//        // Set click listener for record button
//        recordButton.setOnClickListener {
//            toggleRecording()
//        }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Preview
//            preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
//                }
//
//            // VideoCapture
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            // ImageAnalysis for barcode scanning
//            imageAnalysis = ImageAnalysis.Builder()
//                .build()
//                .also {
//                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
//                }
//
//            // Select back camera
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll()
//
//                // Bind use cases to camera
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, videoCapture, imageAnalysis
//                )
//
//            } catch (exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startRecording() {
//        val videoCapture = this.videoCapture ?: return
//
//        val name = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, "BarcodeVideo_$name")
//            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/BarcodeVideos")
//            }
//        }
//
//        val outputOptions = MediaStoreOutputOptions.Builder(
//            contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        ).setContentValues(contentValues).build()
//
//        recording = videoCapture.output.prepareRecording(this, outputOptions)
//            .withAudioEnabled()
//            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
//                when (recordEvent) {
//                    is VideoRecordEvent.Start -> {
//                        recordingStartTime = System.currentTimeMillis()
//                        Log.d(TAG, "Recording started")
//                    }
//                    is VideoRecordEvent.Finalize -> {
//                        if (!recordEvent.hasError()) {
//                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
//                            Log.d(TAG, msg)
//                            videoFileUri = recordEvent.outputResults.outputUri
//                            navigateToSearchActivity()
//                        } else {
//                            recording?.close()
//                            recording = null
//                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
//                        }
//                    }
//                }
//            }
//    }
//
//    private fun stopRecording() {
//        recording?.stop()
//        recording = null
//    }
//
//    private fun toggleRecording() {
//        if (isRecording) {
//            // Stop recording
//            stopRecording()
//            recordButton.setImageResource(R.drawable.ic_record)
//            isRecording = false
//            recordingDuration.visibility = View.GONE
//            handler.removeCallbacks(updateDurationRunnable)
//        } else {
//            // Start recording
//            startRecording()
//            recordButton.setImageResource(R.drawable.ic_stop)
//            isRecording = true
//            recordingDuration.visibility = View.VISIBLE
//            recordingStartTime = System.currentTimeMillis()
//            updateRecordingDuration()
//        }
//    }
//
//    private fun updateRecordingDuration() {
//        updateDurationRunnable = object : Runnable {
//            override fun run() {
//                val currentTime = System.currentTimeMillis()
//                val elapsedMillis = currentTime - recordingStartTime
//                val seconds = (elapsedMillis / 1000) % 60
//                val minutes = (elapsedMillis / (1000 * 60)) % 60
//                val duration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
//                recordingDuration.text = duration
//                handler.postDelayed(this, 1000)
//            }
//        }
//        handler.post(updateDurationRunnable)
//    }
//
//    private fun handleDetectedBarcode(barcode: Barcode) {
//        val barcodeValue = barcode.rawValue ?: return
//        val currentTime = System.currentTimeMillis()
//        val timestamp = currentTime - recordingStartTime
//
//        val barcodeData = BarcodeData(
//            value = barcodeValue,
//            timestamp = timestamp
//        )
//        detectedBarcodes.add(barcodeData)
//
//        runOnUiThread {
//            Toast.makeText(this, "Barcode detected: $barcodeValue", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
//        private val scanner = BarcodeScanning.getClient()
//
//        @androidx.camera.core.ExperimentalGetImage
//        override fun analyze(imageProxy: ImageProxy) {
//            val mediaImage = imageProxy.image
//            if (mediaImage != null) {
//                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//                scanner.process(image)
//                    .addOnSuccessListener { barcodes ->
//                        for (barcode in barcodes) {
//                            handleDetectedBarcode(barcode)
//                        }
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e(TAG, "Barcode analysis error", e)
//                    }
//                    .addOnCompleteListener {
//                        imageProxy.close()
//                    }
//            } else {
//                imageProxy.close()
//            }
//        }
//    }
//
//    private fun navigateToSearchActivity() {
//        val intent = Intent(this, BarcodeSearchActivity::class.java)
//        intent.putExtra("videoUri", videoFileUri.toString())
//        intent.putExtra("barcodeDataList", ArrayList(detectedBarcodes))
//        startActivity(intent)
//        finish()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopRecording()
//        cameraExecutor.shutdown()
//    }
//
//    override fun onClick(v: View?) {
//        // Handle button clicks if needed
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults: IntArray
//    ) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera()
//            } else {
//                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "LiveBarcodeActivity"
//    }
//}

//
//package com.google.mlkit.md
//
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Intent
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.*
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.common.InputImage
//import java.text.SimpleDateFormat
//import java.util.*
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class LiveBarcodeScanningActivity : AppCompatActivity(), View.OnClickListener {
//
//    private var preview: Preview? = null
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var imageAnalysis: ImageAnalysis? = null
//    private lateinit var cameraExecutor: ExecutorService
//    private var recording: Recording? = null
//    private var recordingStartTime: Long = 0L
//    private var videoFileUri: Uri? = null
//    private val detectedBarcodes = mutableListOf<BarcodeData>()
//
//    private val REQUIRED_PERMISSIONS = arrayOf(
//        android.Manifest.permission.CAMERA,
//        android.Manifest.permission.RECORD_AUDIO
//    )
//    private val REQUEST_CODE_PERMISSIONS = 10
//
//    private lateinit var recordButton: FloatingActionButton
//    private lateinit var recordingDuration: TextView
//    private var isRecording = false
//    private val handler = Handler()
//    private lateinit var updateDurationRunnable: Runnable
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_live_barcode)
//
//        // Request permissions
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//            )
//        }
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        // Initialize record button and recording duration TextView
//        recordButton = findViewById(R.id.record_button)
//        recordingDuration = findViewById(R.id.recording_duration)
//
//        // Set click listener for record button
//        recordButton.setOnClickListener {
//            toggleRecording()
//        }
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Preview
//            preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
//                }
//
//            // VideoCapture
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            // ImageAnalysis for barcode scanning
//            imageAnalysis = ImageAnalysis.Builder()
//                .build()
//                .also {
//                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
//                }
//
//            // Select back camera
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll()
//
//                // Bind use cases to camera
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, videoCapture, imageAnalysis
//                )
//
//            } catch (exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startRecording() {
//        val videoCapture = this.videoCapture ?: return
//
//        val name = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, "BarcodeVideo_$name")
//            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/BarcodeVideos")
//            }
//        }
//
//        val outputOptions = MediaStoreOutputOptions.Builder(
//            contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        ).setContentValues(contentValues).build()
//
//        recording = videoCapture.output.prepareRecording(this, outputOptions)
//            .withAudioEnabled()
//            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
//                when (recordEvent) {
//                    is VideoRecordEvent.Start -> {
//                        recordingStartTime = System.currentTimeMillis()
//                        Log.d(TAG, "Recording started")
//                    }
//                    is VideoRecordEvent.Finalize -> {
//                        if (!recordEvent.hasError()) {
//                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
//                            Log.d(TAG, msg)
//                            videoFileUri = recordEvent.outputResults.outputUri
//                            navigateToSearchActivity()
//                        } else {
//                            recording?.close()
//                            recording = null
//                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
//                        }
//                    }
//                }
//            }
//    }
//
//    private fun stopRecording() {
//        recording?.stop()
//        recording = null
//    }
//
//    private fun toggleRecording() {
//        if (isRecording) {
//            // Stop recording
//            stopRecording()
//            recordButton.setImageResource(R.drawable.ic_record)
//            isRecording = false
//            recordingDuration.visibility = View.GONE
//            handler.removeCallbacks(updateDurationRunnable)
//        } else {
//            // Start recording
//            startRecording()
//            recordButton.setImageResource(R.drawable.ic_stop)
//            isRecording = true
//            recordingDuration.visibility = View.VISIBLE
//            recordingStartTime = System.currentTimeMillis()
//            updateRecordingDuration()
//        }
//    }
//
//    private fun updateRecordingDuration() {
//        updateDurationRunnable = object : Runnable {
//            override fun run() {
//                val currentTime = System.currentTimeMillis()
//                val elapsedMillis = currentTime - recordingStartTime
//                val seconds = (elapsedMillis / 1000) % 60
//                val minutes = (elapsedMillis / (1000 * 60)) % 60
//                val duration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
//                recordingDuration.text = duration
//                handler.postDelayed(this, 1000)
//            }
//        }
//        handler.post(updateDurationRunnable)
//    }
//
//    private fun handleDetectedBarcode(barcode: Barcode) {
//        if (!isRecording) return  // Only detect barcodes when recording
//
//        val barcodeValue = barcode.rawValue ?: return
//        val currentTime = System.currentTimeMillis()
//        val timestamp = currentTime - recordingStartTime
//
//        val barcodeData = BarcodeData(
//            value = barcodeValue,
//            timestamp = timestamp
//        )
//        detectedBarcodes.add(barcodeData)
//
//        // Update the Toast message immediately with the newly detected barcode
//        runOnUiThread {
//            Toast.makeText(this, "Barcode detected: $barcodeValue", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
//        private val scanner = BarcodeScanning.getClient()
//
//        @androidx.camera.core.ExperimentalGetImage
//        override fun analyze(imageProxy: ImageProxy) {
//            val mediaImage = imageProxy.image
//            if (mediaImage != null) {
//                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//                scanner.process(image)
//                    .addOnSuccessListener { barcodes ->
//                        for (barcode in barcodes) {
//                            handleDetectedBarcode(barcode)
//                        }
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e(TAG, "Barcode analysis error", e)
//                    }
//                    .addOnCompleteListener {
//                        imageProxy.close()
//                    }
//            } else {
//                imageProxy.close()
//            }
//        }
//    }
//
//    private fun navigateToSearchActivity() {
//        val intent = Intent(this, BarcodeSearchActivity::class.java)
//        intent.putExtra("videoUri", videoFileUri.toString())
//        intent.putExtra("barcodeDataList", ArrayList(detectedBarcodes))
//        startActivity(intent)
//        finish()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopRecording()
//        cameraExecutor.shutdown()
//    }
//
//    override fun onClick(v: View?) {
//        // Handle button clicks if needed
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults: IntArray
//    ) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera()
//            } else {
//                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    companion object {
//        private const val TAG = "LiveBarcodeActivity"
//    }
//}
//
//package com.google.mlkit.md
//
//import android.annotation.SuppressLint
//import android.content.ContentValues
//import android.content.Intent
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.video.*
//import androidx.camera.view.PreviewView
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.mlkit.md.settings.SettingsActivity
//import com.google.mlkit.vision.barcode.common.Barcode
//import com.google.mlkit.vision.barcode.BarcodeScanning
//import com.google.mlkit.vision.common.InputImage
//import java.text.SimpleDateFormat
//import java.util.*
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//
//class LiveBarcodeScanningActivity : AppCompatActivity(), View.OnClickListener {
//
//    private var preview: Preview? = null
//    private var videoCapture: VideoCapture<Recorder>? = null
//    private var imageAnalysis: ImageAnalysis? = null
//    private lateinit var cameraExecutor: ExecutorService
//    private var recording: Recording? = null
//    private var recordingStartTime: Long = 0L
//    private var videoFileUri: Uri? = null
//    private val detectedBarcodes = mutableListOf<BarcodeData>()
//    private var camera: Camera? = null // To control camera features
//
//    private val REQUIRED_PERMISSIONS = arrayOf(
//        android.Manifest.permission.CAMERA,
//        android.Manifest.permission.RECORD_AUDIO
//    )
//    private val REQUEST_CODE_PERMISSIONS = 10
//
//    private lateinit var recordButton: FloatingActionButton
//    private lateinit var recordingDuration: TextView
//    private lateinit var closeButton: ImageView
//    private lateinit var settingsButton: ImageView
//    private lateinit var flashButton: ImageView
//    private var isRecording = false
//    private var isFlashOn = false
//    private val handler = Handler()
//    private lateinit var updateDurationRunnable: Runnable
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_live_barcode)
//
//        // Request permissions
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
//            )
//        }
//
//        cameraExecutor = Executors.newSingleThreadExecutor()
//
//        // Initialize buttons and recording duration TextView
//        recordButton = findViewById(R.id.record_button)
//        recordingDuration = findViewById(R.id.recording_duration)
//        closeButton = findViewById(R.id.close_button)
//        settingsButton = findViewById(R.id.settings_button)
//        flashButton = findViewById(R.id.flash_button)
//
//        // Set click listeners for buttons
//        recordButton.setOnClickListener { toggleRecording() }
//        closeButton.setOnClickListener(this)
//        settingsButton.setOnClickListener(this)
//        flashButton.setOnClickListener(this)
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Preview
//            preview = Preview.Builder()
//                .build()
//                .also {
//                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
//                }
//
//            // VideoCapture
//            val recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder)
//
//            // ImageAnalysis for barcode scanning
//            imageAnalysis = ImageAnalysis.Builder()
//                .build()
//                .also {
//                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
//                }
//
//            // Select back camera
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll()
//
//                // Bind use cases to camera
//                camera = cameraProvider.bindToLifecycle(
//                    this, cameraSelector, preview, videoCapture, imageAnalysis
//                )
//
//            } catch (exc: Exception) {
//                Log.e(TAG, "Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startRecording() {
//        val videoCapture = this.videoCapture ?: return
//
//        val name = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
//            .format(System.currentTimeMillis())
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, "BarcodeVideo_$name")
//            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/BarcodeVideos")
//            }
//        }
//
//        val outputOptions = MediaStoreOutputOptions.Builder(
//            contentResolver,
//            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//        ).setContentValues(contentValues).build()
//
//        recording = videoCapture.output.prepareRecording(this, outputOptions)
//            .withAudioEnabled()
//            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
//                when (recordEvent) {
//                    is VideoRecordEvent.Start -> {
//                        recordingStartTime = System.currentTimeMillis()
//                        Log.d(TAG, "Recording started")
//                    }
//                    is VideoRecordEvent.Finalize -> {
//                        if (!recordEvent.hasError()) {
//                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
//                            Log.d(TAG, msg)
//                            videoFileUri = recordEvent.outputResults.outputUri
//                            navigateToSearchActivity()
//                        } else {
//                            recording?.close()
//                            recording = null
//                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
//                        }
//                    }
//                }
//            }
//    }
//
//    private fun stopRecording() {
//        recording?.stop()
//        recording = null
//    }
//
//    private fun toggleRecording() {
//        if (isRecording) {
//            // Stop recording
//            stopRecording()
//            recordButton.setImageResource(R.drawable.ic_record)
//            isRecording = false
//            recordingDuration.visibility = View.GONE
//            handler.removeCallbacks(updateDurationRunnable)
//        } else {
//            // Start recording
//            startRecording()
//            recordButton.setImageResource(R.drawable.ic_stop)
//            isRecording = true
//            recordingDuration.visibility = View.VISIBLE
//            recordingStartTime = System.currentTimeMillis()
//            updateRecordingDuration()
//        }
//    }
//
//    private fun updateRecordingDuration() {
//        updateDurationRunnable = object : Runnable {
//            override fun run() {
//                val currentTime = System.currentTimeMillis()
//                val elapsedMillis = currentTime - recordingStartTime
//                val seconds = (elapsedMillis / 1000) % 60
//                val minutes = (elapsedMillis / (1000 * 60)) % 60
//                val duration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
//                recordingDuration.text = duration
//                handler.postDelayed(this, 1000)
//            }
//        }
//        handler.post(updateDurationRunnable)
//    }
//
//    private fun handleDetectedBarcode(barcode: Barcode) {
//        if (!isRecording) return  // Only detect barcodes when recording
//
//        val barcodeValue = barcode.rawValue ?: return
//        val currentTime = System.currentTimeMillis()
//        val timestamp = currentTime - recordingStartTime
//
//        val barcodeData = BarcodeData(
//            value = barcodeValue,
//            timestamp = timestamp
//        )
//        detectedBarcodes.add(barcodeData)
//
//        // Update the Toast message immediately with the newly detected barcode
//        runOnUiThread {
//            Toast.makeText(this, "Barcode detected: $barcodeValue", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
//        private val scanner = BarcodeScanning.getClient()
//
//        @androidx.camera.core.ExperimentalGetImage
//        override fun analyze(imageProxy: ImageProxy) {
//            val mediaImage = imageProxy.image
//            if (mediaImage != null) {
//                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//                scanner.process(image)
//                    .addOnSuccessListener { barcodes ->
//                        for (barcode in barcodes) {
//                            handleDetectedBarcode(barcode)
//                        }
//                    }
//                    .addOnFailureListener { e ->
//                        Log.e(TAG, "Barcode analysis error", e)
//                    }
//                    .addOnCompleteListener {
//                        imageProxy.close()
//                    }
//            } else {
//                imageProxy.close()
//            }
//        }
//    }
//
//    private fun navigateToSearchActivity() {
//        val intent = Intent(this, BarcodeSearchActivity::class.java)
//        intent.putExtra("videoUri", videoFileUri.toString())
//        intent.putExtra("barcodeDataList", ArrayList(detectedBarcodes))
//        startActivity(intent)
//        finish()
//    }
//
//    override fun onClick(v: View?) {
//        when (v?.id) {
//            R.id.close_button -> onBackPressed()
//            R.id.settings_button -> {
//                startActivity(Intent(this, SettingsActivity::class.java))
//            }
//            R.id.flash_button -> {
//                camera?.let {
//                    isFlashOn = !isFlashOn
//                    it.cameraControl.enableTorch(isFlashOn)
//                    flashButton.isSelected = isFlashOn
//                }
//            }
//        }
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults: IntArray
//    ) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                startCamera()
//            } else {
//                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopRecording()
//        cameraExecutor.shutdown()
//    }
//
//    companion object {
//        private const val TAG = "LiveBarcodeActivity"
//    }
//}

package com.google.mlkit.md

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.md.settings.SettingsActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LiveBarcodeScanningActivity : AppCompatActivity(), View.OnClickListener {

    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageAnalysis: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private var recording: Recording? = null
    private var recordingStartTime: Long = 0L
    private var videoFileUri: Uri? = null
    private val detectedBarcodes = mutableListOf<BarcodeData>()
    private var camera: Camera? = null // To control camera features

    private val REQUIRED_PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )
    private val REQUEST_CODE_PERMISSIONS = 10

    private lateinit var recordButton: FloatingActionButton
    private lateinit var recordingDuration: TextView
    private lateinit var closeButton: ImageView
    private lateinit var settingsButton: ImageView
    private lateinit var flashButton: ImageView
    private lateinit var bottomPromptChip: Chip
    private var isRecording = false
    private var isFlashOn = false
    private val handler = Handler()
    private lateinit var updateDurationRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_barcode)

        // Request permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize buttons, chip, and recording duration TextView
        recordButton = findViewById(R.id.record_button)
        recordingDuration = findViewById(R.id.recording_duration)
        closeButton = findViewById(R.id.close_button)
        settingsButton = findViewById(R.id.settings_button)
        flashButton = findViewById(R.id.flash_button)
        bottomPromptChip = findViewById(R.id.bottom_prompt_chips)

        // Set click listeners for buttons
        recordButton.setOnClickListener { toggleRecording() }
        closeButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        flashButton.setOnClickListener(this)

        // Set initial prompt text
        updatePromptText()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
                }

            // VideoCapture
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // ImageAnalysis for barcode scanning
            imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer())
                }

            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        val videoCapture = this.videoCapture ?: return

        val name = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "BarcodeVideo_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/BarcodeVideos")
            }
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        recording = videoCapture.output.prepareRecording(this, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        recordingStartTime = System.currentTimeMillis()
                        Log.d(TAG, "Recording started")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            Log.d(TAG, msg)
                            videoFileUri = recordEvent.outputResults.outputUri
                            navigateToSearchActivity()
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    private fun toggleRecording() {
        if (isRecording) {
            // Stop recording
            stopRecording()
            recordButton.setImageResource(R.drawable.ic_record)
            isRecording = false
            recordingDuration.visibility = View.GONE
            handler.removeCallbacks(updateDurationRunnable)
        } else {
            // Start recording
            startRecording()
            recordButton.setImageResource(R.drawable.ic_stop)
            isRecording = true
            recordingDuration.visibility = View.VISIBLE
            recordingStartTime = System.currentTimeMillis()
            updateRecordingDuration()
        }
        // Update the prompt text whenever recording state changes
        updatePromptText()
    }

    private fun updateRecordingDuration() {
        updateDurationRunnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val elapsedMillis = currentTime - recordingStartTime
                val seconds = (elapsedMillis / 1000) % 60
                val minutes = (elapsedMillis / (1000 * 60)) % 60
                val duration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                recordingDuration.text = duration
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateDurationRunnable)
    }

    private fun updatePromptText() {
        bottomPromptChip.post {
            if (isRecording) {
                bottomPromptChip.text = "Show me Barcode"
            } else {
                bottomPromptChip.text = "Hey, Click There --->>"
            }
        }
    }

    private fun handleDetectedBarcode(barcode: Barcode) {
        if (!isRecording) return  // Only detect barcodes when recording

        val barcodeValue = barcode.rawValue ?: return
        val currentTime = System.currentTimeMillis()
        val timestamp = currentTime - recordingStartTime

        val barcodeData = BarcodeData(
            value = barcodeValue,
            timestamp = timestamp
        )
        detectedBarcodes.add(barcodeData)

        // Update the Toast message immediately with the newly detected barcode
        runOnUiThread {
            Toast.makeText(this, "Barcode detected: $barcodeValue", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient()

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            handleDetectedBarcode(barcode)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Barcode analysis error", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    private fun navigateToSearchActivity() {
        val intent = Intent(this, BarcodeSearchActivity::class.java)
        intent.putExtra("videoUri", videoFileUri.toString())
        intent.putExtra("barcodeDataList", ArrayList(detectedBarcodes))
        startActivity(intent)
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.close_button -> onBackPressed()
            R.id.settings_button -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.flash_button -> {
                camera?.let {
                    isFlashOn = !isFlashOn
                    it.cameraControl.enableTorch(isFlashOn)
                    flashButton.isSelected = isFlashOn
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "LiveBarcodeActivity"
    }
}
