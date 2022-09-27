package com.xenione.digit

import android.graphics.Camera
import android.graphics.Matrix

/**
 * Created by Eugeni on 16/10/2016.
 */
object MatrixHelper {
    private val camera = Camera()

    /**
     * Matrix with 180 degrees x rotation defined
     */
    val MIRROR_X = Matrix()

    init {
        rotateX(MIRROR_X, 180)
    }

    /**
     * Matrix with 0 degrees x rotation defined
     */
    val ROTATE_X_0 = Matrix()

    init {
        rotateX(ROTATE_X_0, 0)
    }

    /**
     * Matrix with 90 degrees x rotation defined
     */
    val ROTATE_X_90 = Matrix()

    init {
        rotateX(ROTATE_X_90, 90)
    }

    fun mirrorX(matrix: Matrix?) {
        rotateX(matrix, 180)
    }

    fun rotateX(matrix: Matrix?, alpha: Int) {
        synchronized(camera) {
            camera.save()
            camera.rotateX(alpha.toFloat())
            camera.getMatrix(matrix)
            camera.restore()
        }
    }

    fun rotateZ(matrix: Matrix?, alpha: Int) {
        synchronized(camera) {
            camera.save()
            camera.rotateZ(alpha.toFloat())
            camera.getMatrix(matrix)
            camera.restore()
        }
    }

    fun translate(matrix: Matrix?, dx: Float, dy: Float, dz: Float) {
        synchronized(camera) {
            camera.save()
            camera.translate(dx, dy, dz)
            camera.getMatrix(matrix)
            camera.restore()
        }
    }

    fun translateY(matrix: Matrix?, dy: Float) {
        synchronized(camera) {
            camera.save()
            camera.translate(0f, dy, 0f)
            camera.getMatrix(matrix)
            camera.restore()
        }
    }
}