package com.gummybearstudio.infapp.backend

class SuperpixelBox(topPixel: Int, leftPixel: Int, bottomPixel: Int, rightPixel: Int) {
    private val _topPixel = topPixel
    private val _leftPixel = leftPixel
    private val _bottomPixel = bottomPixel
    private val _rightPixel = rightPixel

    val topPixel
        get() = _topPixel
    val leftPixel
        get() = _leftPixel
    val bottomPixel
        get() = _bottomPixel
    val rightPixel
        get() = _rightPixel

    constructor(pixels: List<Int>) : this(pixels[0], pixels[1], pixels[2], pixels[3])
}