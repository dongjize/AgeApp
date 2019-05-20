package com.example.ageapp.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.media.FaceDetector
import android.util.AttributeSet
import android.util.Log
import android.view.View

class MediaFaceDetectView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    //两眼之间的距离
    private var mEyesDistance: Float = 0.toFloat()
    //实际检测到的人脸数
    private var mNumberOfFaceDetected: Int = 0
    private var mBitmap: Bitmap? = null
    private var mImageWidth: Int = 0
    private var mImageHeight: Int = 0
    //最大检测的人脸数
    private val mMaxNumberOfFace = 5
    //人脸识别类的实例
    private var mFaceDetect: FaceDetector? = null
    //存储多张人脸的数组变量
    private lateinit var mFaces: Array<FaceDetector.Face?>
    private var mPaint: Paint? = null
    private val mPointF = PointF()

    fun init() {
        mPaint = Paint()
        mPaint!!.color = Color.WHITE
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = 3f
    }

    fun setBitmap(bitmap: Bitmap) {
        this.mBitmap = bitmap
        mImageWidth = mBitmap!!.width
        mImageHeight = mBitmap!!.height
        mFaces = arrayOfNulls(mMaxNumberOfFace)
        mFaceDetect = FaceDetector(mImageWidth, mImageHeight, mMaxNumberOfFace)
        mNumberOfFaceDetected = mFaceDetect!!.findFaces(mBitmap, mFaces)
        Log.e("jupiter", "mNumberOfFaceDetected is $mNumberOfFaceDetected")
        invalidate()
    }

//    fun init(drawable: Int) {
//        mPaint = Paint()
//        mPaint!!.color = Color.WHITE
//        mPaint!!.style = Paint.Style.STROKE
//        mPaint!!.strokeWidth = 3f
//        val options = BitmapFactory.Options()
//        options.inPreferredConfig = Bitmap.Config.RGB_565  //必须为565
//        mBitmap = BitmapFactory.decodeResource(resources, drawable, options)
//        mImageWidth = mBitmap!!.width
//        mImageHeight = mBitmap!!.height
//        mFaces = emptyArray()
//        mFaceDetect = FaceDetector(mImageWidth, mImageHeight, mMaxNumberOfFace)
//        mNumberOfFaceDetected = mFaceDetect!!.findFaces(mBitmap, mFaces)
//        Log.e("jupiter", "mNumberOfFaceDetected is $mNumberOfFaceDetected")
//        invalidate()
//    }

    override fun onDraw(canvas: Canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap!!, 0f, 0f, null)

            for (i in 0 until mNumberOfFaceDetected) {
                val face = mFaces[i]
                face!!.getMidPoint(mPointF)
                mEyesDistance = face.eyesDistance()
                canvas.drawRect(
                    (mPointF.x - mEyesDistance).toInt().toFloat(),
                    (mPointF.y - mEyesDistance).toInt().toFloat(),
                    (mPointF.x + mEyesDistance).toInt().toFloat(),
                    (mPointF.y + mEyesDistance).toInt().toFloat(),
                    mPaint!!
                )
            }
        }

    }

}
