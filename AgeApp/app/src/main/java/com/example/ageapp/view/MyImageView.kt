package com.example.ageapp.view

import android.content.Context
import android.graphics.*
import android.media.FaceDetector
import android.util.AttributeSet
import android.util.Log
import com.example.ageapp.R

class MyImageView : android.support.v7.widget.AppCompatImageView {
    //实际检测到的人脸数
    private var mNumberOfFaceDetected: Int = 0
    //最大检测的人脸数
    private val mMaxNumberOfFace = 5
    //人脸识别类的实例
    private var mFaceDetect: FaceDetector? = null
    //存储多张人脸的数组变量
    private lateinit var mFaces: Array<FaceDetector.Face?>
    private var mPaint: Paint? = null
    private val paint = Paint()

    private lateinit var mPointFList: Array<PointF?>

    //两眼之间的距离
    private lateinit var mEyesDistances: Array<Float?>

    private var mBitmap: Bitmap? = null

    //    private var imgBg: ImageView? = null
    private var mContext: Context? = null


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
        mPaint!!.strokeWidth = 5f

        paint.color = Color.RED
        paint.strokeWidth = 5f
        paint.style = Paint.Style.FILL
        paint.textSize = textSize

    }

    fun setImgBitmap(bitmap: Bitmap) {
        this.mBitmap = bitmap
        setImageBitmap(bitmap)
    }

    fun detectFace(): Pair<Array<PointF?>, Array<Float?>>? {
        if (mBitmap == null) {
            return null
        }

        mFaces = arrayOfNulls(mMaxNumberOfFace)
        mFaceDetect = FaceDetector(mBitmap!!.width, mBitmap!!.height, mMaxNumberOfFace)
        mNumberOfFaceDetected = mFaceDetect!!.findFaces(mBitmap, mFaces)
        Log.e("jupiter", "mNumberOfFaceDetected is $mNumberOfFaceDetected")

        mPointFList = arrayOfNulls(mMaxNumberOfFace)
        mEyesDistances = arrayOfNulls(mMaxNumberOfFace)
        for (i in 0 until mNumberOfFaceDetected) {
            val face = mFaces[i]
            mPointFList[i] = PointF()
            face!!.getMidPoint(mPointFList[i])
            mEyesDistances[i] = face.eyesDistance()

//            canvas.drawRect(
//                (mPointF.x - mEyesDistance).toInt().toFloat(),
//                (mPointF.y - mEyesDistance).toInt().toFloat(),
//                (mPointF.x + mEyesDistance).toInt().toFloat(),
//                (mPointF.y + mEyesDistance).toInt().toFloat(),
//                mPaint!!
//            )
        }
        return Pair(mPointFList, mEyesDistances)
    }


    private var textStringList: ArrayList<String> = ArrayList()
    private var textSize = 60f

    fun setDrawText(ageList: ArrayList<String>) {
        for (i in ageList.indices) {
            textStringList.add(ageList[i])
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mBitmap != null) {


            var ww = measuredWidth.toFloat() / mBitmap!!.width.toFloat()
            var hh = measuredHeight.toFloat() / mBitmap!!.height.toFloat()
            var ratio = if (ww > hh) {
                hh
            } else {
                ww
            }

            var realW = mBitmap!!.width * ratio
            var realH = mBitmap!!.height * ratio

            var marginW = measuredWidth / 2 - realW / 2
            var marginH = measuredHeight / 2 - realH / 2

//            Log.e("this.marginW", marginW.toString())
//            Log.e("this.marginH", marginH.toString())

            if (textStringList.size > 0) {
                for (i in textStringList.indices) {
//                canvas.drawText(textStringList[i], mPointFList[i]!!.x, mPointFList[i]!!.y, paint)
                    canvas.drawText(
                        textStringList[i],
                        marginW + (mPointFList[i]!!.x + mEyesDistances[i]!! * 1.2f) * ratio,
                        marginH + (mPointFList[i]!!.y - mEyesDistances[i]!! * 1.2f) * ratio,
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
