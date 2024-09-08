package com.gummybearstudio.infapp.backend
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.gummybearstudio.infapp.ml.MobileObjectLocalizerV1
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import java.nio.ByteBuffer

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
        val outputs = this.model!!.process(preprocessInput(input))
        return outputs
    }

    fun prepareInference() {
        this.model = MobileObjectLocalizerV1.newInstance(this.context);
    }

    fun closeInference() {
        this.model?.close()
        this.model = null;
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