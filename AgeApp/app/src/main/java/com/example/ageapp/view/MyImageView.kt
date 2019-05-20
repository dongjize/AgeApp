package com.example.ageapp.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.FaceDetector
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.Log

class MyImageView : AppCompatImageView {
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
    private var mPaint: Paint = Paint()
    private val mPointF = PointF()

    constructor(context: Context) : super(context) {
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 3f
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mNumberOfFaceDetected > 0) {
            for (i in 0 until mNumberOfFaceDetected) {
                val face = mFaces[i]
                face!!.getMidPoint(mPointF)
                mEyesDistance = face.eyesDistance()
                canvas.drawRect(0F, 0F, 400F, 400F, mPaint);
//                canvas.drawRect(
//                    (mPointF.x - mEyesDistance).toInt().toFloat(),
//                    (mPointF.y - mEyesDistance).toInt().toFloat(),
//                    (mPointF.x + mEyesDistance).toInt().toFloat(),
//                    (mPointF.y + mEyesDistance).toInt().toFloat(),
//                    mPaint
//                )
            }
        }

    }


    fun showAge() {
        mBitmap = (this.drawable as BitmapDrawable).bitmap
        mImageWidth = mBitmap!!.width
        mImageHeight = mBitmap!!.height
        mFaces = arrayOfNulls(mMaxNumberOfFace)
        mFaceDetect = FaceDetector(mImageWidth, mImageHeight, mMaxNumberOfFace)
        mNumberOfFaceDetected = mFaceDetect!!.findFaces(mBitmap, mFaces)

        Log.e("detected: ", mNumberOfFaceDetected.toString())

        invalidate()
    }
}
