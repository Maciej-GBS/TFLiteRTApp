package com.gummybearstudio.infapp.backend
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.gummybearstudio.infapp.ml.MobileObjectLocalizerV1
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer
import kotlin.system.measureTimeMillis

class ObjectDetectionHandler(
    context: android.content.Context) {

    private var context: android.content.Context
    private var listeners: MutableList<ResultListener> = mutableListOf()

    init {
        this.context = context
    }

    private var model: MobileObjectLocalizerV1? = null

    fun addListener(listener: ResultListener) {
        this.listeners.add(listener)
    }

    fun runInference(input: Bitmap): MobileObjectLocalizerV1.Outputs {
        val (outputs, timeMs) = measureExecution {
            this.model!!.process(preprocessInput(input))
        }
        listeners.forEach {
            it.onResults(postprocessOutput(outputs), timeMs)
        }
        return outputs
    }

    fun prepareInference() {
        this.model = MobileObjectLocalizerV1.newInstance(this.context);
    }

    fun closeInference() {
        this.model?.close()
        this.model = null;
    }

    private fun postprocessOutput(outputs: MobileObjectLocalizerV1.Outputs): List<DetectedObject> {
        val n = InterpretBuffer.intFromBytes(outputs.outputFeature0AsTensorBuffer.buffer)
        return List(n) { _ ->
            val box = InterpretBuffer.intListFromBytes(outputs.outputFeature1AsTensorBuffer.buffer)
            val score = InterpretBuffer.floatFromBytes(outputs.outputFeature2AsTensorBuffer.buffer)
            DetectedObject(SuperpixelBox(box), score, ENTITY_CLASS)
        }
    }

    private fun preprocessInput(inputBitmap: Bitmap): TensorBuffer {
        val scaledBitmap = inputBitmap.scale(WIDTH, HEIGHT)
        val imageBytes: ByteBuffer = TensorImage.fromBitmap(scaledBitmap).buffer
        val buffer = TensorBuffer.createFixedSize(
                intArrayOf(1, WIDTH, HEIGHT, 3), DataType.UINT8).apply {
            loadBuffer(imageBytes)
        }
        return buffer
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

        const val ENTITY_CLASS = 1
    }

}