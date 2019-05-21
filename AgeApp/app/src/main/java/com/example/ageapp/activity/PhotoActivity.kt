package com.example.ageapp.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.InputStream
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import com.example.ageapp.dialog.ConfirmationDialog
import com.example.android.camera2basic.ErrorDialog
import java.io.File
import java.io.FileInputStream
import android.media.ExifInterface
import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import com.example.ageapp.*
import com.example.ageapp.util.ImageUtils
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.IOException


class PhotoActivity : AppCompatActivity(), View.OnClickListener {

    private var bitmap: Bitmap? = null
    private var filePath: String? = null

    private val ageModelPath = "file:///android_asset/keras_model_01.pb"
    private val inputName = "input_1"
    private val outputName1 = "output_1"
    private val outputName2 = "output_2"
    private var tf: TensorFlowInferenceInterface? = null

    private var floatValues: FloatArray? = null
    private var agePredictionList = FloatArray(1000)
    private var genderPredictionList = FloatArray(1000)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

//        val childView = LayoutInflater.from(this).inflate(R.layout.layout_imgview_point, null, false) as FrameLayout
//        myImageView.addView(childView)

        analyzeBtn.setOnClickListener(this)
        takePhoto.setOnClickListener(this)
        selectAlbum.setOnClickListener(this)

        tf = TensorFlowInferenceInterface(assets, ageModelPath)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.analyzeBtn -> {
                if (bitmap != null) {
                    val detectedBitmaps = detectFace(bitmap!!)
                    if (detectedBitmaps != null) {
//                        myImageView.addPoints()
                        predict(detectedBitmaps)
                    } else {
                        showToast("Error")
                    }
                } else {
                    showToast("Please take or choose a photo")
                }
            }

            R.id.takePhoto -> {
                filePath = Environment.getExternalStorageDirectory().path + "/" + System.currentTimeMillis() + ".jpg"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    takePhotoLaterThan7((File(filePath)).absolutePath)
                } else {
                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    val uri = Uri.fromFile(File(filePath))
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    if (takePhotoIntent.resolveActivity(packageManager) != null) {
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST)
                    }
                }
            }

            R.id.selectAlbum -> {
                val choosePhotoIntent = Intent()
                choosePhotoIntent.type = "image/*"
                choosePhotoIntent.action = Intent.ACTION_GET_CONTENT
                if (choosePhotoIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(Intent.createChooser(choosePhotoIntent, "select pic"), SELECT_ALBUM_REQUEST)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            TAKE_PHOTO_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val inStream: InputStream = FileInputStream(filePath)

                    val options = BitmapFactory.Options()
                    options.inPreferredConfig = Bitmap.Config.RGB_565
                    options.inMutable = false
                    bitmap = rotateImageView(
                        readPictureDegree(filePath!!),
                        BitmapFactory.decodeStream(inStream, null, options)
                    )
                    myImageView.setImgBitmap(bitmap!!)


                    val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        requestWriteStoragePermission()
                        return
                    }
                    // store the photo
                    MediaStore.Images.Media.insertImage(contentResolver, bitmap, "title", "description")

                }
            }

            SELECT_ALBUM_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri: Uri? = data.data
                    if (uri != null) {
                        val inStream: InputStream?
                        try {
                            inStream = contentResolver.openInputStream(uri)
                            val options = BitmapFactory.Options()
                            options.inPreferredConfig = Bitmap.Config.RGB_565
                            options.inMutable = false
                            bitmap = BitmapFactory.decodeStream(inStream, null, options)
                            myImageView.setImgBitmap(bitmap!!)
                        } catch (e: Exception) {
                            showToast(e.message!!)
                        }
                    }
                }
            }
        }
    }


    private fun requestWriteStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ConfirmationDialog().show(supportFragmentManager, "dialog")
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                    .show(supportFragmentManager, "dialog")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation =
                exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    private fun rotateImageView(angle: Int, bitmap: Bitmap?): Bitmap {
        var returnBm: Bitmap? = null
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        try {
            returnBm = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: OutOfMemoryError) {
        }

        if (returnBm == null) {
            returnBm = bitmap
        }
        if (bitmap != returnBm) {
            bitmap!!.recycle()
        }
        return returnBm!!
    }


    fun argmax(array: FloatArray): Array<Any> {
        var best = -1
        var bestConfidence = 0.0f

        for (i in array.indices) {
            val value = array[i]
            if (value > bestConfidence) {
                bestConfidence = value
                best = i
            }
        }

        return arrayOf(best, bestConfidence)
    }

    private fun detectFace(bitmap: Bitmap): ArrayList<Bitmap?>? {
        val pair = myImageView.detectFace() ?: return null
        val pointFList: Array<PointF?> = pair.first
        val eyesDistances: Array<Float?> = pair.second
        val scale = 1.2
        val newBitmaps: ArrayList<Bitmap?> = ArrayList()
        for (i in pointFList.indices) {
            if (pointFList[i] != null && eyesDistances[i] != null) {
                val pointF = pointFList[i]
                val eyesDistance = eyesDistances[i]

                val newBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap,
                    (pointF!!.x - eyesDistance!! * scale).toInt(),
                    (pointF.y - eyesDistance * scale * 0.8).toInt(),
                    (2 * eyesDistance * scale).toInt(),
                    (2 * eyesDistance * scale).toInt()
                )
                newBitmaps.add(newBitmap)
            }

        }

        return newBitmaps
    }

    private var ageList: ArrayList<String> = ArrayList()

    private fun predict(bitmaps: ArrayList<Bitmap?>?) {
        if (bitmaps != null) {
            for (i in bitmaps.indices) {
                //Resize  the  image  into  64  x  64
                val resizedImage = ImageUtils.processBitmap(bitmaps[i], 64)

                //Normalize  the  pixels
                floatValues = ImageUtils.normalizeBitmap(resizedImage, 64, 127.5f, 1.0f)

                assert(tf != null)
                //Pass  input  into  the  tensorflow
                tf!!.feed(inputName, floatValues, 1, 64, 64, 3)

                //compute  agePredictionList
                tf!!.run(arrayOf(outputName1, outputName2))

                //copy  the  output  into  the  agePredictionList  array
                tf!!.fetch(outputName1, genderPredictionList)
                tf!!.fetch(outputName2, agePredictionList)

                //Obtained  highest  prediction
                val results = argmax(agePredictionList)
                val age = results[0] as Int
                val confidence = results[1] as Float

                val haha = argmax(genderPredictionList)
                val gender = haha[0] as Int


                val conf = (confidence * 100).toString().substring(0, 5)
                //Convert  predicted  class  index  into  actual  label  name

                Log.e("RESULT ".plus(i), age.toString())
                ageList.add(age.toString())
                showToast(age.toString().plus(" ").plus(conf).plus(" ").plus(gender))
            }
            myImageView.setDrawText(ageList)

        }

    }
}