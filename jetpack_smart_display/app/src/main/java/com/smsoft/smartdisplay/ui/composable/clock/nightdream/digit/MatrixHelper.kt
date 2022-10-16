package com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit

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
        rotateX(
            matrix = ROTATE_X_0,
            alpha = 0
        )
    }

    /**
     * Matrix with 90 degrees x rotation defined
     */
    val ROTATE_X_90 = Matrix()

    init {
        rotateX(
            matrix = ROTATE_X_90,
            alpha = 90
        )
    }

    fun rotateX(
        matrix: Matrix,
        alpha: Int
    ) {
        synchronized(camera) {
            camera.apply {
                save()
                rotateX(alpha.toFloat())
                getMatrix(matrix)
                restore()
            }
        }
    }

    fun translate(
        matrix: Matrix,
        dx: Float,
        dy: Float,
        dz: Float
    ) {
        synchronized(camera) {
            camera.apply {
                save()
                translate(dx, dy, dz)
                getMatrix(matrix)
                restore()
            }
        }
    }
}