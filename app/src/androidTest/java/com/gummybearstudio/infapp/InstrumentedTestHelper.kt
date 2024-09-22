package com.gummybearstudio.infapp

import android.content.Context
import java.io.File

object InstrumentedTestHelper {

    fun getRawModelAsFile(context: Context, rawResourceId: Int): File {
        val outputFileName = "instrumented_test_model.tflite"
        val outputFile = File(context.filesDir, outputFileName)

        context.resources.openRawResource(rawResourceId).use { inputStream ->
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return outputFile
    }

}