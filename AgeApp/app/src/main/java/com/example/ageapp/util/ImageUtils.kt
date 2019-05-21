package com.example.ageapp.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import org.json.JSONObject

import java.io.InputStream

object ImageUtils {
    /**
     * Returns a transformation matrix from one reference frame into another.
     * Handles cropping (if maintaining aspect ratio is desired) and rotation.
     *
     * @param srcWidth            Width of source frame.
     * @param srcHeight           Height of source frame.
     * @param dstWidth            Width of destination frame.
     * @param dstHeight           Height of destination frame.
     * @param applyRotation       Amount of rotation to apply from one frame to another.
     * Must be a multiple of 90.
     * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
     * cropping the image if necessary.
     * @return The transformation fulfilling the desired requirements.
     */
    fun getTransformationMatrix(
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int,
        applyRotation: Int,
        maintainAspectRatio: Boolean
    ): Matrix {
        val matrix = Matrix()

        if (applyRotation != 0) {
            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        val transpose = (Math.abs(applyRotation) + 90) % 180 == 0

        val inWidth = if (transpose) srcHeight else srcWidth
        val inHeight = if (transpose) srcWidth else srcHeight

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            val scaleFactorX = dstWidth / inWidth.toFloat()
            val scaleFactorY = dstHeight / inHeight.toFloat()

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
                matrix.postScale(scaleFactor, scaleFactor)
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY)
            }
        }

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
        }

        return matrix
    }


    fun processBitmap(source: Bitmap?, size: Int): Bitmap {

        val image_height = source!!.height
        val image_width = source!!.width

        val croppedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val frameToCropTransformations = getTransformationMatrix(image_width, image_height, size, size, 0, false)
        val cropToFrameTransformations = Matrix()
        frameToCropTransformations.invert(cropToFrameTransformations)

        val canvas = Canvas(croppedBitmap)
        canvas.drawBitmap(source!!, frameToCropTransformations, null)

        return croppedBitmap


    }

    fun normalizeBitmap(source: Bitmap, size: Int, mean: Float, std: Float): FloatArray {

        val output = FloatArray(size * size * 3)

        val intValues = IntArray(source.height * source.width)

        source.getPixels(intValues, 0, source.width, 0, 0, source.width, source.height)
        for (i in intValues.indices) {
            val `val` = intValues[i]
            output[i * 3] = ((`val` shr 16 and 0xFF) - mean) / std
            output[i * 3 + 1] = ((`val` shr 8 and 0xFF) - mean) / std
            output[i * 3 + 2] = ((`val` and 0xFF) - mean) / std
        }

        return output

    }

    fun argmax(array: FloatArray): Array<Any> {


        var best = -1
        var best_confidence = 0.0f

        for (i in array.indices) {

            val value = array[i]

            if (value > best_confidence) {

                best_confidence = value
                best = i
            }
        }


        return arrayOf(best, best_confidence)


    }


    fun getLabel(jsonStream: InputStream, index: Int): String {
        var label = ""
        try {
            val jsonData = ByteArray(jsonStream.available())
            jsonStream.read(jsonData)
            jsonStream.close()

            val jsonString = String(jsonData)
            val `object` = JSONObject(jsonString)

            label = `object`.getString(index.toString())


        } catch (e: Exception) {


        }

        return label
    }
}