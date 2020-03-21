package com.tedb.looper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val PERMISSION_REQUEST_CODE = 1234

class MainActivity : AppCompatActivity() {

    var recordManager : RecordManager? = null
    var recordButton : Button? = null
    var isRecording : Boolean = false

    fun updateButtonText(isRecording: Boolean) {
        recordButton?.text = if (isRecording) getString(R.string.stop_button_text) else getString(R.string.record_button_text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permission_status = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        if (permission_status!= PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this,
                permissions,
                PERMISSION_REQUEST_CODE);
        }
        else {
            recordManager = RecordManager() {
                updateButtonText(it)
                isRecording = it
            }
        }

        recordButton = findViewById<Button>(R.id.record_button)
        recordButton?.setOnClickListener() {
            if (isRecording) {
                recordManager?.stopRecording()
            }
            else {
                recordManager?.startRecording()
            }
        }

        val clearButton = findViewById<Button>(R.id.clear_button)
        clearButton.setOnClickListener() {
            recordManager?.clearRecordings()
        }
    }
}
