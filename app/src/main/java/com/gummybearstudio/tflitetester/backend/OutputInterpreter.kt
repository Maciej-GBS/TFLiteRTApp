package com.gummybearstudio.tflitetester.backend

import android.util.Log
import java.nio.ByteBuffer

object OutputInterpreter {
    private const val ENTITY_CLASS = 1

    /*
    0: java.nio.DirectByteBuffer[pos=1600 lim=1600 cap=1600] 400x 32bit float : boxes
    1: java.nio.DirectByteBuffer[pos=400 lim=400 cap=400] 100x 32bit float : classes
    2: java.nio.DirectByteBuffer[pos=400 lim=400 cap=400] 100x 32bit float : scores
    3: java.nio.DirectByteBuffer[pos=4 lim=4 cap=4] - 32bit int : n of detections
     */
    fun toDetectedObjects(outputs: Map<Int, ByteBuffer>): List<DetectedObject> {
        val feature0 = outputs.getValue(0).apply { rewind() }.asFloatBuffer()
        val feature2 = outputs.getValue(2).apply { rewind() }.asFloatBuffer()
        val feature3 = outputs.getValue(3).apply { rewind() }.asFloatBuffer()
        Log.d("OutputInterpreter", "received outputs: $outputs")
        val n = feature3.get().toInt()
        Log.d("OutputInterpreter", "n objects: $n")
        return List(n) {
            val boxArray = FloatArray(4)
            feature0.get(boxArray)
            val box = SuperpixelBox(boxArray)
            val score = feature2.get()
            val dObj = DetectedObject(box, score, ENTITY_CLASS)
            dObj
        }
    }
}