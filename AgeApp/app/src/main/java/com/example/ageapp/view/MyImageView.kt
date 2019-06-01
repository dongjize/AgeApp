package com.example.ageapp.view

import android.content.Context
import android.graphics.*
import android.media.FaceDetector
import android.util.AttributeSet
import android.util.Log
import com.example.ageapp.R

class MyImageView : android.support.v7.widget.AppCompatImageView {
    private var mNumberOfFaceDetected: Int = 0
    private val mMaxNumberOfFace = 5
    private var mFaceDetect: FaceDetector? = null
    private lateinit var mFaces: Array<FaceDetector.Face?>
    private var mPaint: Paint? = null
    private val paint = Paint()
    private val paint2 = Paint()

    private lateinit var mPointFList: Array<PointF?>
    private lateinit var mEyesDistances: Array<Float?>
    private lateinit var confidences: Array<Float?>

    private var mBitmap: Bitmap? = null

    private var mContext: Context? = null

    private var textStringList: ArrayList<String> = ArrayList()
    private var textSize = 40f


    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        mContext = context

        mPaint = Paint()
        mPaint!!.color = Color.RED
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = 8f

        // text
        paint.color = Color.WHITE
        paint.strokeWidth = 8f
        paint.style = Paint.Style.FILL
        paint.textSize = textSize

        // small rect
        paint2.color = Color.RED
        paint2.style = Paint.Style.FILL

    }

    fun setImgBitmap(bitmap: Bitmap) {
        this.mBitmap = bitmap
        setImageBitmap(bitmap)
    }

    fun detectFace(): Triple<Array<PointF?>, Array<Float?>, Array<Float?>>? {
        if (mBitmap == null) {
            return null
        }
        textStringList = ArrayList()

        mFaces = arrayOfNulls(mMaxNumberOfFace)
        mFaceDetect = FaceDetector(mBitmap!!.width, mBitmap!!.height, mMaxNumberOfFace)
        mNumberOfFaceDetected = mFaceDetect!!.findFaces(mBitmap, mFaces)

        mPointFList = arrayOfNulls(mMaxNumberOfFace)
        mEyesDistances = arrayOfNulls(mMaxNumberOfFace)
        confidences = arrayOfNulls(mMaxNumberOfFace)
        for (i in 0 until mNumberOfFaceDetected) {
            val face = mFaces[i]
            mPointFList[i] = PointF()
            face!!.getMidPoint(mPointFList[i])
            mEyesDistances[i] = face.eyesDistance()
            confidences[i] = face.confidence()
            Log.e("confidence", "".plus(confidences[i]))
            Log.e("eye distance", "".plus(mEyesDistances[i]))

        }
        return Triple(mPointFList, mEyesDistances, confidences)
    }


    fun setDrawText(ageList: FloatArray) {
        for (i in ageList.indices) {
            textStringList.add(ageList[i].toInt().toString())
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mBitmap != null) {

            val ww = measuredWidth.toFloat() / mBitmap!!.width.toFloat()
            val hh = measuredHeight.toFloat() / mBitmap!!.height.toFloat()
            val ratio = if (ww > hh) {
                hh
            } else {
                ww
            }

            val realW = mBitmap!!.width * ratio
            val realH = mBitmap!!.height * ratio

            val marginW = measuredWidth / 2 - realW / 2
            val marginH = measuredHeight / 2 - realH / 2


            if (textStringList.size > 0) {
                for (i in textStringList.indices) {
                    canvas.drawRect(
                        marginW + (mPointFList[i]!!.x + mEyesDistances[i]!! * 1.2f) * ratio - 60,
                        marginH + (mPointFList[i]!!.y - mEyesDistances[i]!! * 1.2f) * ratio,
                        marginW + (mPointFList[i]!!.x + mEyesDistances[i]!! * 1.2f) * ratio,
                        marginH + (mPointFList[i]!!.y - mEyesDistances[i]!! * 1.2f) * ratio + 60,
                        paint2
                    )
                    canvas.drawText(
                        textStringList[i],
                        marginW + (mPointFList[i]!!.x + mEyesDistances[i]!! * 1.2f) * ratio - 50,
                        marginH + (mPointFList[i]!!.y - mEyesDistances[i]!! * 1.2f) * ratio + 50,
                        paint
                    )
                    canvas.drawRect(
                        marginW + (mPointFList[i]!!.x - mEyesDistances[i]!! * 1.2f) * ratio,
                        marginH + (mPointFList[i]!!.y - mEyesDistances[i]!! * 1.2f) * ratio,
                        marginW + (mPointFList[i]!!.x + mEyesDistances[i]!! * 1.2f) * ratio,
                        marginH + (mPointFList[i]!!.y + mEyesDistances[i]!! * 1.2f) * ratio,
                        mPaint!!
                    )

                }

            }
        }

    }
}
