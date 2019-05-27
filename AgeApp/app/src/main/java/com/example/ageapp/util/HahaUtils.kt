package com.example.ageapp.util

import android.graphics.*

object HahaUtils {
    fun getGrayBitmap(bm: Bitmap): Bitmap {
        var bitmap: Bitmap? = null
        //获取图片的宽和高
        val width = bm.width
        val height = bm.height
        //创建灰度图片
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        //创建画布
        val canvas = Canvas(bitmap!!)
        //创建画笔
        val paint = Paint()
        //创建颜色矩阵
        val matrix = ColorMatrix()
        //设置颜色矩阵的饱和度:0代表灰色,1表示原图
        matrix.setSaturation(0f)
        //颜色过滤器
        val cmcf = ColorMatrixColorFilter(matrix)
        //设置画笔颜色过滤器
        paint.colorFilter = cmcf
        //画图
        canvas.drawBitmap(bm, 0f, 0f, paint)
        return bitmap
    }
}
