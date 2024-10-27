package com.gummybearstudio.tflitetester.backend

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gummybearstudio.tflitetester.R
import java.io.File
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object ModelFileProvider {
    private const val INTERNAL_FILE = "last_model.tflite"

    private var _modelFile: File? = null

    val modelFile: File?
        get() = _modelFile

    fun readModelBuffer(): MappedByteBuffer {
        val file = _modelFile ?: throw IllegalStateException(
            "Programming fault: model should be loaded first")
        return file.inputStream().use { stream ->
            stream.channel.use { channel ->
                channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
            }
        }
    }

    fun launchDialog(senderFragment: Fragment, onFileProvided: () -> Unit) {
        _modelFile?.let {
            onFileProvided()
            return
        }

        val openDocumentLauncher = senderFragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                _modelFile = when {
                    uri?.scheme == ContentResolver.SCHEME_CONTENT -> {
                        Log.d("ModelFileProvider", "Path: ${uri.path!!}")
                        val inputStream =
                            senderFragment.requireContext().contentResolver.openInputStream(uri)
                        val internalFile = File(
                            senderFragment.requireContext().filesDir, INTERNAL_FILE)
                        inputStream?.use { stream ->
                            internalFile.outputStream().use { outputStream ->
                                stream.copyTo(outputStream)
                            }
                        }
                        internalFile
                    }
                    else -> {
                        Log.d("ModelFileProvider", "Unsupported scheme: ${uri?.scheme}")
                        null
                    }
                }
            }
            onFileProvided()
        }

        val context = senderFragment.requireContext()
        useDefaultModelAlert(context, {
            // onPositive
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            openDocumentLauncher.launch(intent)
        }, {
            // onNegative
            val outputFile = File(context.filesDir, INTERNAL_FILE)
            context.resources.openRawResource(R.raw.mobile_object_localizer_v1).use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            _modelFile = outputFile
            openDocumentLauncher.unregister()
            onFileProvided()
        })
    }

    private fun useDefaultModelAlert(context: Context, onPositive: () -> Unit, onNegative: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.model_selection_title)
        builder.setMessage(R.string.model_selection_prompt)

        builder.setPositiveButton(R.string.model_selection_ok) { _, _ ->
            Toast.makeText(context, R.string.model_selection_ok_toast, Toast.LENGTH_SHORT).show()
            onPositive()
        }

        builder.setNegativeButton(R.string.model_selection_no) { _, _ ->
            Toast.makeText(context, R.string.model_selection_no_toast, Toast.LENGTH_SHORT).show()
            onNegative()
        }

        builder.show()
    }

}