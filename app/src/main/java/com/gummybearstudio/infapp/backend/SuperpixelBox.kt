package com.gummybearstudio.infapp.backend

class SuperpixelBox(topPos: Float, leftPos: Float, bottomPos: Float, rightPos: Float) {
    private val _topPos = topPos
    private val _leftPos = leftPos
    private val _bottomPos = bottomPos
    private val _rightPos = rightPos

    val topPos
        get() = _topPos
    val leftPos
        get() = _leftPos
    val bottomPos
        get() = _bottomPos
    val rightPos
        get() = _rightPos

    constructor(pixels: List<Float>) : this(pixels[0], pixels[1], pixels[2], pixels[3])

    fun project(width: Float, height: Float) = SuperpixelBox(
        _topPos * height,
        _leftPos * width,
        _bottomPos * height,
        _rightPos * width)

    fun isProjected(): Boolean {
        return topPos > 1f || leftPos > 1f || bottomPos > 1f || rightPos > 1f
    }

    override fun toString(): String {
        return "SBox{$topPos, $leftPos, $bottomPos, $rightPos}"
    }
}