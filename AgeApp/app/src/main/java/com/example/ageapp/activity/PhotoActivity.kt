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
import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import com.example.ageapp.*
import com.example.ageapp.util.ImageUtils
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.*
import kotlin.collections.ArrayList


class PhotoActivity : AppCompatActivity(), View.OnClickListener {

    private var bitmap: Bitmap? = null
    private var filePath: String? = null

    private val ageModelPath = "file:///android_asset/ssr_model.pb"
    //    private val ageModelPath = "file:///android_asset/resnet_model.pb"
    private val inputName = "input_1"
    private val outputName = "output_1"
    private var tf: TensorFlowInferenceInterface? = null

    private var inputValues: FloatArray? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

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
                        ImageUtils.readPictureDegree(filePath!!),
                        BitmapFactory.decodeStream(inStream, null, options)
                    )
//                    bitmap = BitmapFactory.decodeStream(inStream, null, options)
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


    private fun rotateImageView(angle: Int, bitmap: Bitmap?): Bitmap {
        var returnBm: Bitmap? = null
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        try {
            returnBm = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
        }

        if (returnBm == null) {
            returnBm = bitmap
        }
        if (bitmap != returnBm) {
            bitmap!!.recycle()
        }
        return returnBm!!
    }


    private fun detectFace(bitmap: Bitmap): ArrayList<Bitmap>? {
        val pair = myImageView.detectFace() ?: return null
        val pointFList: Array<PointF?> = pair.first
        val eyesDistances: Array<Float?> = pair.second
        val scale = 1.2
        val newBitmaps: ArrayList<Bitmap> = ArrayList()
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


    private fun predict(bitmaps: ArrayList<Bitmap>?) {

        if (bitmaps != null) {
            val agePredictionList = FloatArray(bitmaps.size)
            Log.e("bitmaps size", bitmaps.size.toString())


            //Resize  the  image  into  64  x  64
            val resizedImages = ImageUtils.processBitmap2(bitmaps, 64)

//            val intValues = IntArray(64 * 64)
//            resizedImages[0]!!.getPixels(intValues, 0, 64, 0, 0, 64, 64)

            //Normalize  the  pixels
            inputValues = ImageUtils.normalizeBitmap2(resizedImages, 64, 127.5f, 1.0f)

            assert(tf != null)
            //Pass  input  into  the  tensorflow
            tf!!.feed(inputName, inputValues, resizedImages.size.toLong(), 64, 64, 3)

            //compute  agePredictionList
            tf!!.run(arrayOf(outputName))

            //copy  the  output  into  the  agePredictionList  array
            tf!!.fetch(outputName, agePredictionList)

            myImageView.setDrawText(agePredictionList)

        }
    }
}