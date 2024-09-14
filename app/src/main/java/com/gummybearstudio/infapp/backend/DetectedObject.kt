package com.gummybearstudio.infapp.backend

class DetectedObject(detectionBox: SuperpixelBox,
                     detectionScore: Float,
                     detectionClass: Int) {

    private val _box: SuperpixelBox = detectionBox
    private val _score: Float = detectionScore
    private val _classId: Int = detectionClass

    val box
        get() = _box
    val score
        get() = _score
    val classId
        get() = _classId

    override fun toString(): String {
        val boxStr = box.toString()
        return "DetectedObject(box=$boxStr, score=$score, classId=$classId)"
    }
}
