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
     * X rotation seen from the shared default camera. Only use it for 0 and 180 degrees: those keep
     * the plane flat, so the camera distance does not matter.
     */
    fun rotateX(
        matrix: Matrix,
        alpha: Int
    ) {
        synchronized(camera) {
            rotateX(
                camera = camera,
                matrix = matrix,
                alpha = alpha
            )
        }
    }

    /**
     * X rotation seen from [camera]. Any other angle is drawn in perspective, so the camera
     * distance has to fit the size of what is rotated.
     */
    fun rotateX(
        camera: Camera,
        matrix: Matrix,
        alpha: Int
    ) {
        camera.apply {
            save()
            rotateX(alpha.toFloat())
            getMatrix(matrix)
            restore()
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