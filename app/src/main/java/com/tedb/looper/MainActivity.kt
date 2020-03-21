package com.tedb.looper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

const val PERMISSION_REQUEST_CODE = 1234
const val COUNTDOWN_PERIOD_MILLIS = 500

class MainActivity : AppCompatActivity() {

    var recordManager : RecordManager? = null
    var recordButton : Button? = null
    var isRecording : Boolean = false

    fun updateButtonText(isRecording: Boolean) {
        runOnUiThread {
            recordButton?.text =
                if (isRecording) getString(R.string.stop_button_text)
                else getString(R.string.record_button_text)
        }
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
                recordManager?.onStopRecordingButton()
            }
            else {
                val toast = Toast.makeText(this,"",Toast.LENGTH_SHORT)
                thread(start=true) {
                    for (i in 1..4) {
                        runOnUiThread {
                            toast.setText(i.toString())
                            toast.show()
                        }
                        Thread.sleep(COUNTDOWN_PERIOD_MILLIS.toLong())
                    }
                    runOnUiThread {toast.cancel()}
                    recordManager?.startRecording()
                }
            }
        }

        val clearButton = findViewById<Button>(R.id.clear_button)
        clearButton.setOnClickListener() {
            recordManager?.clearRecordings()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    // re-instantiating the record manager will create a new audio recorder
                    recordManager = RecordManager() {
                        updateButtonText(it)
                        isRecording = it
                    }
                }
            }
        }
    }
}
