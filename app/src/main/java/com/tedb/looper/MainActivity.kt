package com.tedb.looper

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.tedb.looper.audio.RecordManager
import com.tedb.looper.audio.RecordState
import java.io.File
import kotlin.concurrent.thread

const val PERMISSION_REQUEST_CODE = 1234
const val COUNTDOWN_PERIOD_MILLIS = 500



class MainActivity : AppCompatActivity() {

    private var recordManager : RecordManager? = null
    private var recordButton : Button? = null
    private var isRecording : Boolean = false
    private var enableCountDown : Boolean = false

    private fun getCountdownEnabled() : Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getBoolean("countdown",true)
    }

    private fun getButtonColor(isRecording:Boolean) : Int {
        if (isRecording) {
            return getColor(R.color.colorAccent)
        }
        else {
            return getColor(R.color.colorBg)
        }
    }

    fun updateButtonStyle() {
        runOnUiThread {
            if (recordManager==null) return@runOnUiThread
            val recordState = recordManager!!.getRecordState()
            if (recordState == RecordState.RECORDING_FIRST) {
                recordButton!!.text = getString(R.string.stop_button_text)
            }
            else if (recordState == RecordState.RECORDING_LAYER){
                recordButton!!.text = getString(R.string.recording_button_text)
            }
            else if (recordState == RecordState.NOT_RECORDING) {
                recordButton!!.text = getString(R.string.record_button_text)
            }

            val color = getButtonColor(recordState!=RecordState.NOT_RECORDING)
            recordButton!!.background.setTint(color)
        }
    }

    override fun onResume() {
        super.onResume()
        enableCountDown = getCountdownEnabled()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableCountDown = getCountdownEnabled()

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
                updateButtonStyle()
                isRecording = it
            }
        }

        recordButton = findViewById<Button>(R.id.record_button)
        recordButton?.setOnClickListener() {
            if (isRecording) {
                recordManager?.onStopRecordingButton()
            }
            else {
                // stop audio before count in
                recordManager?.stopPlayback()

                // give count in
                val toast = Toast.makeText(this,"",Toast.LENGTH_SHORT)

                // run on separate thread because we need to wait
                thread(start=true) {
                    if (enableCountDown) {
                        for (i in 1..4) {
                            runOnUiThread {
                                toast.setText(i.toString())
                                toast.show()
                            }
                            Thread.sleep(COUNTDOWN_PERIOD_MILLIS.toLong())
                        }
                        runOnUiThread { toast.cancel() }
                    }
                    recordManager?.startRecording()
                }
            }
        }
        val file = File(Environment.getExternalStorageDirectory().path+"/layeredlooper/")
        file.mkdir()
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
                        updateButtonStyle()
                        isRecording = it
                    }
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_settings) {
            startActivity(Intent(this,SettingsActivity::class.java))
            return true
        }
        if (item.itemId == R.id.menu_clear) {
            recordManager?.clearRecordings()
            return true
        }
        if (item.itemId == R.id.menu_share) {
            if (recordManager==null) return true
            val file = recordManager!!.saveToFile()
            val newpath = Environment.getExternalStorageDirectory().path+"/layeredlooper/temp.wav"
            Log.d("record","saving in "+newpath)
            file.renameTo(File(newpath))
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM,file.toUri())
                type="audio/*"
            }
            val shareIntent = Intent.createChooser(sendIntent,null)
            startActivity(shareIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
