package com.gummybearstudio.infapp.backend

class SuperpixelBox(top: Float, left: Float, bottom: Float, right: Float) {
    private val _top = top
    private val _left = left
    private val _bottom = bottom
    private val _right = right

    val top
        get() = _top
    val left
        get() = _left
    val bottom
        get() = _bottom
    val right
        get() = _right

    constructor(pixels: List<Float>) : this(pixels[0], pixels[1], pixels[2], pixels[3])

    fun project(width: Float, height: Float) = SuperpixelBox(
        _top * height,
        _left * width,
        _bottom * height,
        _right * width)

    fun isProjected(): Boolean {
        return top > 1f || left > 1f || bottom > 1f || right > 1f
    }

    override fun toString(): String {
        return "SBox{top=$top, left=$left, bottom=$bottom, right=$right}"
    }
}