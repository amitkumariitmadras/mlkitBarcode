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
//import android.app.Activity
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//
///** Entry activity to select the detection mode.  */
//class MainActivity : AppCompatActivity() {
//
//    private enum class DetectionMode(val titleResId: Int, val subtitleResId: Int) {
//        ODT_LIVE(R.string.mode_odt_live_title, R.string.mode_odt_live_subtitle),
//        ODT_STATIC(R.string.mode_odt_static_title, R.string.mode_odt_static_subtitle),
//        BARCODE_LIVE(R.string.mode_barcode_live_title, R.string.mode_barcode_live_subtitle),
//        CUSTOM_MODEL_LIVE(R.string.custom_model_live_title, R.string.custom_model_live_subtitle)
//    }
//
//    override fun onCreate(bundle: Bundle?) {
//        super.onCreate(bundle)
//
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
//        setContentView(R.layout.activity_main)
//        findViewById<RecyclerView>(R.id.mode_recycler_view).apply {
//            setHasFixedSize(true)
//            layoutManager = LinearLayoutManager(this@MainActivity)
//            adapter = ModeItemAdapter(DetectionMode.values())
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (!Utils.allPermissionsGranted(this)) {
//            Utils.requestRuntimePermissions(this)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == Utils.REQUEST_CODE_PHOTO_LIBRARY &&
//            resultCode == Activity.RESULT_OK &&
//            data != null
//        ) {
//            val intent = Intent(this, StaticObjectDetectionActivity::class.java)
//            intent.data = data.data
//            startActivity(intent)
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }
//
//    private inner class ModeItemAdapter internal constructor(private val detectionModes: Array<DetectionMode>) :
//        RecyclerView.Adapter<ModeItemAdapter.ModeItemViewHolder>() {
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeItemViewHolder {
//            return ModeItemViewHolder(
//                LayoutInflater.from(parent.context)
//                    .inflate(
//                        R.layout.detection_mode_item, parent, false
//                    )
//            )
//        }
//
//        override fun onBindViewHolder(modeItemViewHolder: ModeItemViewHolder, position: Int) =
//            modeItemViewHolder.bindDetectionMode(detectionModes[position])
//
//        override fun getItemCount(): Int = detectionModes.size
//
//        private inner class ModeItemViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
//
//            private val titleView: TextView = view.findViewById(R.id.mode_title)
//            private val subtitleView: TextView = view.findViewById(R.id.mode_subtitle)
//
//            internal fun bindDetectionMode(detectionMode: DetectionMode) {
//                titleView.setText(detectionMode.titleResId)
//                subtitleView.setText(detectionMode.subtitleResId)
//                itemView.setOnClickListener {
//                    val activity = this@MainActivity
//                    when (detectionMode) {
//                        DetectionMode.ODT_LIVE ->
//                            activity.startActivity(Intent(activity, LiveObjectDetectionActivity::class.java))
//                        DetectionMode.ODT_STATIC -> Utils.openImagePicker(activity)
//                        DetectionMode.BARCODE_LIVE ->
//                            activity.startActivity(Intent(activity, LiveBarcodeScanningActivity::class.java))
//                        DetectionMode.CUSTOM_MODEL_LIVE ->
//                            activity.startActivity(Intent(activity, CustomModelObjectDetectionActivity::class.java))
//                    }
//                }
//            }
//        }
//    }
//}
//
//package com.google.mlkit.md
//
//import android.content.ContentValues.TAG
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
//
///** Main activity that starts the live barcode scanning activity directly. */
//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(bundle: Bundle?) {
//        super.onCreate(bundle)
//        Log.d(TAG, "MainActivity onCreate called")
//
//        // Start the LiveBarcodeScanningActivity directly
//        val intent = Intent(this, LiveBarcodeScanningActivity::class.java)
//        startActivity(intent)
//
//        finish() // Close MainActivity so it's not in the back stack
//    }
//}

package com.google.mlkit.md

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** Main activity that starts the live barcode scanning activity directly. */
class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS = 100

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        Log.d(TAG, "MainActivity onCreate called")

        // Check for camera and audio permissions before starting LiveBarcodeScanningActivity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // If permissions are already granted, start LiveBarcodeScanningActivity
            startLiveBarcodeActivity()
        } else {
            // Request permissions if they are not granted
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            REQUEST_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "All permissions granted")
                // Permissions granted, start the next activity
                startLiveBarcodeActivity()
            } else {
                Log.e(TAG, "Permissions denied. Cannot proceed.")
                // Permissions denied, show a message to the user and close the app
                finish() // Close MainActivity to avoid proceeding without permissions
            }
        }
    }

    private fun startLiveBarcodeActivity() {
        Log.d(TAG, "Starting LiveBarcodeScanningActivity")
        val intent = Intent(this, LiveBarcodeScanningActivity::class.java)
        startActivity(intent)

        // Close MainActivity so it's not in the back stack
        finish()
    }
}

