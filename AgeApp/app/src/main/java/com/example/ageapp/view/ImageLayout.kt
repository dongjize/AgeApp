package com.example.ageapp.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.media.FaceDetector
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.ageapp.R

class ImageLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {
    //两眼之间的距离
    private var mEyesDistance: Float = 0.toFloat()
    //实际检测到的人脸数
    private var mNumberOfFaceDetected: Int = 0
    //最大检测的人脸数
    private val mMaxNumberOfFace = 5
    //人脸识别类的实例
    private var mFaceDetect: FaceDetector? = null
    //存储多张人脸的数组变量
    private lateinit var mFaces: Array<FaceDetector.Face?>
    private var mPaint: Paint? = null
    private val mPointF = PointF()

    private var mBitmap: Bitmap? = null

    private var imgBg: ImageView? = null
    private var mContext: Context? = null
    private var layoutPoints: FrameLayout? = null

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        mContext = context

        val imgPointLayout = View.inflate(context, R.layout.layout_imgview_point, this)

        imgBg = imgPointLayout.findViewById(R.id.imgBg)

//        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val metrics = DisplayMetrics()
//        wm.defaultDisplay.getRealMetrics(metrics)
//        var w: Int = metrics.widthPixels
//        var h: Int = metrics.heightPixels
//        val lp = imgBg!!.layoutParams
//        lp.width = w
//        lp.height = h
//        imgBg!!.layoutParams = lp
//        Log.e("aaaa === ", w.toString().plus(" ").plus(h.toString()))

        layoutPoints = imgPointLayout.findViewById(R.id.layoutPoints)

        mPaint = Paint()
        mPaint!!.color = Color.WHITE
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = 3f

    }

    fun setImgBitmap(bitmap: Bitmap) {
        this.mBitmap = bitmap
        imgBg!!.setImageBitmap(bitmap)
    }


    fun detectFace(): Pair<PointF, Float>? {
//        layoutPoints!!.removeAllViews()
        if (mBitmap == null) {
            return null
        }

        mFaces = arrayOfNulls(mMaxNumberOfFace)
        mFaceDetect = FaceDetector(mBitmap!!.width, mBitmap!!.height, mMaxNumberOfFace)
        mNumberOfFaceDetected = mFaceDetect!!.findFaces(mBitmap, mFaces)
        Log.e("jupiter", "mNumberOfFaceDetected is $mNumberOfFaceDetected")

        for (i in 0 until mNumberOfFaceDetected) {
            val face = mFaces[i]
            face!!.getMidPoint(mPointF)
            mEyesDistance = face.eyesDistance()
            Log.e("mPointF X === ", mPointF.x.toString())
            Log.e("mPointF Y === ", mPointF.y.toString())
            Log.e("eye distance === ", mEyesDistance.toString())
//            canvas.drawRect(
//                (mPointF.x - mEyesDistance).toInt().toFloat(),
//                (mPointF.y - mEyesDistance).toInt().toFloat(),
//                (mPointF.x + mEyesDistance).toInt().toFloat(),
//                (mPointF.y + mEyesDistance).toInt().toFloat(),
//                mPaint!!
//            )
        }
        return Pair(mPointF, mEyesDistance)
    }

    fun addAgeInfo(age: String) {

    }


}
