package com.android.interviewdemo.utils

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

internal object ComponentsUtils {
}

fun Activity.registerOnBackPressedDispatcher(enabled: Boolean = true, onBackPressed: ()-> Unit) {
    if (Build.VERSION.SDK_INT >= 33) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
            onBackPressed()
        }
    } else {
        (this as ComponentActivity).onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }
}

fun <I, O> Activity.registerForActivityResult(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
) = (this as ComponentActivity).registerForActivityResult(contract, callback)


fun ActivityResultLauncher<Intent>.launchFileChooser(
    type: String,
    title: String = "Choose file",
    multipleAllowed: Boolean = false
) {
    // show file chooser
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = type
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multipleAllowed)
    launch(Intent.createChooser(intent, title))
}

fun Fragment.registerForActivityResult(callback: ((Int, Intent?) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        callback.invoke(result.resultCode, result.data)
    }
}

fun AppCompatActivity.registerForActivityResult(callback: ((Int, Intent?) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        callback.invoke(result.resultCode, result.data)
    }
}

fun Fragment.registerForActivityResultOnSuccess(callback: ((Intent) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result?.data != null) {
            callback.invoke(result.data!!)
        }
    }
}

fun AppCompatActivity.registerForActivityResultOnSuccess(callback: ((Intent) -> Unit)): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result?.data != null) {
            callback.invoke(result.data!!)
        }
    }
}

/**
 * for permissions requests
 * */
fun Fragment.registerForPermissionResult(callback: ((Boolean) -> Unit)): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isGranted?.let { callback.invoke(it) }
    }
}

fun AppCompatActivity.registerForPermissionResult(callback: ((Boolean) -> Unit)): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        isGranted?.let { callback.invoke(it) }
    }
}


fun AppCompatActivity.setLightStatusBar() {
    // set light status bar text color (foreground color)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //  set status text dark
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    window.statusBarColor = ContextCompat.getColor(this, android.R.color.white) // set status background white
}