package com.gummybearstudio.infapp.backend

import java.nio.ByteBuffer

object IntepretBuffer {
    fun intFromBytes(bytes: ByteBuffer): Int {
       return bytes.getInt(0)
    }

    fun floatFromBytes(bytes: ByteBuffer): Float {
        return bytes.getFloat(0)
    }

    fun floatListFromBytes(bytes: ByteBuffer): List<Float> {
        return List(bytes.capacity() / FLOAT_SIZE) { index: Int ->
            bytes.getFloat(index)
        }
    }

    private const val FLOAT_SIZE = 4
}