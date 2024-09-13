package com.gummybearstudio.infapp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gummybearstudio.infapp.backend.ObjectDetectionHandler
import kotlin.random.Random

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.gummybearstudio.infapp", appContext.packageName)
    }

    @Test
    fun runInferenceRandomBitmap() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testObj = ObjectDetectionHandler(appContext)
        var testInput: Bitmap = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888)

        for (x in 0 until testInput.width) {
            for (y in 0 until testInput.height) {
                val randomColor = Random.nextInt(0, 0xFFFFFF) or 0xFF000000.toInt()
                testInput.setPixel(x, y, randomColor)
            }
        }

        testObj.prepareInference()
        testObj.runInference(testInput)
        testObj.closeInference()
    }
}