package com.gummybearstudio.infapp.backend

import java.nio.ByteBuffer

object InterpretBuffer {
    fun intFromBytes(bytes: ByteBuffer): Int {
       return bytes.getInt(0)
    }

    fun intListFromBytes(bytes: ByteBuffer): List<Int> {
        return List(bytes.capacity() / INT_SIZE) { index: Int ->
            bytes.getInt(index)
        }
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
    private const val INT_SIZE = 4
}