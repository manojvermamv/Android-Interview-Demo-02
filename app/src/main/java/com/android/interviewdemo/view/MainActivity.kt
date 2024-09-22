package com.android.interviewdemo.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.interviewdemo.R
import com.android.interviewdemo.databinding.ActivityMainBinding
import com.android.interviewdemo.model.AudioFile
import com.android.interviewdemo.utils.EdgeToEdgeUtils.setLightStatusBar
import com.android.interviewdemo.utils.registerForPermissionResult
import com.android.interviewdemo.view.common.BaseActivity
import com.android.interviewdemo.viewmodel.MainActivityViewModel
import com.android.interviewdemo.viewmodel.MainActivityViewModel.Companion.PLAYING
import com.android.interviewdemo.viewmodel.MainActivityViewModel.Companion.RECORDING
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var viewModel: MainActivityViewModel

    private val audio by lazy { AudioFile(this, "recording") }

    private var alertDialog: AlertDialog? = null

    private val requestRecordAudioPermission = registerForPermissionResult { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            toast("Audio recording permission denied! Go to permissions settings")
            goToSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightStatusBar(window, true)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setSupportActionBar(binding.toolbar)
        title = getString(R.string.audio_recorder)
        setAudioAvailability()

        viewModel.state.observe(this, Observer {
            it?.let { state ->
                when (state) {
                    RECORDING -> {
                        if (alertDialog == null || alertDialog?.isShowing == false) {
                            alertDialog = MaterialAlertDialogBuilder(this@MainActivity).setTitle("Recording sound...").setCancelable(false)
                                .setNegativeButton("Stop") { dialog, _ ->
                                    dialog.dismiss()
                                    binding.fabRecord.performClick()
                                }.create()
                            alertDialog?.show()
                        }
                    }
                    PLAYING -> {
                        if (alertDialog == null || alertDialog?.isShowing == false) {
                            alertDialog = MaterialAlertDialogBuilder(this@MainActivity).setTitle("Playing sound...").setCancelable(false)
                                .setNegativeButton("Stop") { dialog, _ ->
                                    dialog.dismiss()
                                    binding.fabPlay.performClick()
                                }.create()
                            alertDialog?.show()
                        }
                    }
                    else -> {
                        if (alertDialog?.isShowing == true) {
                            alertDialog?.dismiss()
                            alertDialog = null
                            setAudioAvailability()
                        }
                    }
                }
            }
        })

        binding.fabRecord.setOnClickListener {
            if (viewModel.isRecording()) {
                viewModel.stopRecording()
                setAudioAvailability()
                toast("Recording stopped.")
            } else {
                checkRecordAudioPermission()
            }
        }

        binding.fabPlay.setOnClickListener {
            if (viewModel.isPlaying()) {
                viewModel.stopPlayback()
                toast("Stop playing")
            } else {
                toast("Start playing")
                viewModel.startPlayback(audio.getPath())
            }
        }
    }

    private fun setAudioAvailability() {
        if (audio.exists()) {
            binding.textViewTitle.text = "Recording Path: \n${audio.getPath()}"
            binding.fabPlay.visibility = View.VISIBLE
        } else {
            binding.textViewTitle.text = getString(R.string.recording_not_found)
            binding.fabPlay.visibility = View.GONE
        }
    }

    private fun onPermissionGranted() {
        // Permission granted, you can now use the camera
        toast("Recording started.")
        viewModel.startRecording(audio.getPath())
    }

    private fun checkRecordAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> { onPermissionGranted() }
            shouldShowRequestPermissionRationale(permission) -> {
                // permission denied permanently - showPermissionRationaleDialog()
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Permission Required")
                    .setMessage("We need microphone access to record audio.")
                    .setNegativeButton(resources.getString(R.string.decline)) { dialog, which -> dialog.dismiss() }
                    .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                        // Request the permission after the user accepts the rationale
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestRecordAudioPermission.launch(permission)
                        } else {
                            goToSettings()
                        }
                    }.show()
            }
            else -> requestRecordAudioPermission.launch(permission)
        }
    }

    private fun goToSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}