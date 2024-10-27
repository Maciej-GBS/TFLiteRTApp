package com.gummybearstudio.tflitetester.backend

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.system.measureTimeMillis

class ObjectDetectionHandler() {

    private lateinit var _outputMap: Map<Int, ByteBuffer>
    private var listeners: MutableList<ResultListener> = mutableListOf()

    private var model: Interpreter? = null

    fun addListener(listener: ResultListener) {
        this.listeners.add(listener)
    }

    fun runInference(input: Bitmap): Map<Int, ByteBuffer> {
        _outputMap.forEach { (_, value) ->
            value.rewind()
        }
        val (outputBuffer, timeMs) = measureExecution {
            this.model!!.runForMultipleInputsOutputs(
                arrayOf(preprocessInput(input)), _outputMap)
            _outputMap
        }
        Log.d("ObjectDetectionHandler", "Inference time: ${timeMs}ms")
        listeners.forEach {
            val allDetections = OutputInterpreter.toDetectedObjects(outputBuffer)
            it.onResults(allDetections.filter { e -> e.score > THRESHOLD }, timeMs)
        }
        return outputBuffer
    }

    fun prepareInference(modelFile: File) {
        this.model = Interpreter(modelFile)
        this.model!!.allocateTensors()
        val feature0 = ByteBuffer.allocateDirect(400 * 4)
        val feature1 = ByteBuffer.allocateDirect(100 * 4)
        val feature2 = ByteBuffer.allocateDirect(100 * 4)
        val feature3 = ByteBuffer.allocateDirect(4)
        this._outputMap = mapOf(
            0 to feature0,
            1 to feature1,
            2 to feature2,
            3 to feature3
        ).onEach { (_, value) -> value.order(ByteOrder.nativeOrder()) }
    }

    fun closeInference() {
        this.model?.close()
        this.model = null;
    }

    private fun preprocessInput(inputBitmap: Bitmap): ByteBuffer {
        val scaledBitmap = inputBitmap.scale(WIDTH, HEIGHT)
        val imageBytes: ByteBuffer = TensorImage.fromBitmap(scaledBitmap).buffer
        //val buffer = TensorBuffer.createFixedSize(
        //        intArrayOf(1, WIDTH, HEIGHT, 3), DataType.UINT8).apply {
        //    loadBuffer(imageBytes)
        //}
        return imageBytes
    }

    data class Result<T>(val value: T, val time: Long)

    private fun <T> measureExecution(block: () -> T): Result<T> {
        val value: T
        val time = measureTimeMillis {
            value = block()
        }
        return Result(value, time)
    }

    interface ResultListener {
        fun onError(error: String)
        fun onResults(
            results: List<DetectedObject>?,
            inferenceTime: Long
        )
    }

    companion object {
        const val HEIGHT = 192
        const val WIDTH = 192
        const val THRESHOLD = 0.25
    }

}