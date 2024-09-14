package com.gummybearstudio.infapp.backend

import com.gummybearstudio.infapp.backend.ObjectDetectionHandler.Companion.ENTITY_CLASS
import com.gummybearstudio.infapp.ml.MobileObjectLocalizerV1.Outputs

object OutputInterpreter {
    fun toDetectedObjects(outputs: Outputs): List<DetectedObject> {
        /*
        0: java.nio.DirectByteBuffer[pos=1600 lim=1600 cap=1600] 400x 32bit float
        1: java.nio.DirectByteBuffer[pos=400 lim=400 cap=400] 100x 32bit float
        2: java.nio.DirectByteBuffer[pos=400 lim=400 cap=400] 100x 32bit float
        3: java.nio.DirectByteBuffer[pos=4 lim=4 cap=4] - 32bit int
         */
        val n = outputs.outputFeature3AsTensorBuffer.getIntValue(0)
        return List(n) { idx ->
            val box = outputs.outputFeature0AsTensorBuffer.run {
                List(4) { getFloatValue(idx * 4 + it) }
            }
            val score = outputs.outputFeature2AsTensorBuffer.getFloatValue(idx)
            val dObj = DetectedObject(SuperpixelBox(box), score, ENTITY_CLASS)
            dObj
        }
    }
}