package com.example.ageapp.activity

import android.Manifest
import android.app.Activity
import android.content.ContentValues
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
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.ageapp.*
import com.example.ageapp.util.ImageUtils
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import kotlin.collections.ArrayList


class PhotoActivity : AppCompatActivity(), View.OnClickListener {

    private var bitmap: Bitmap? = null
    private var filePath: String? = null

    private var ageModelPath = "file:///android_asset/ssrnet_wiki_model.pb"
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_photo_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.model1 -> {
                ageModelPath = "file:///android_asset/ssrnet_wiki_model.pb"
                tf = TensorFlowInferenceInterface(assets, ageModelPath)
                showToast("model with wiki")
                return true
            }
            R.id.model2 -> {
                ageModelPath = "file:///android_asset/ssrnet_imdb_model.pb"
                tf = TensorFlowInferenceInterface(assets, ageModelPath)
                showToast("model with imdb")
                return true
            }
            R.id.model3 -> {
                ageModelPath = "file:///android_asset/ssrnet_morph2_model.pb"
                tf = TensorFlowInferenceInterface(assets, ageModelPath)
                showToast("model with morph2")
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.analyzeBtn -> {
                if (bitmap != null) {
                    val detectedBitmaps = detectFace(bitmap!!)
                    if (detectedBitmaps != null && detectedBitmaps.size > 0) {
                        predict(detectedBitmaps)
                    } else {
                        showToast("No face detected")
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


    private fun takePhotoLaterThan7(absolutePath: String) {
        val mCameraTempUri: Uri
        try {
            val values = ContentValues(1)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            values.put(MediaStore.Images.Media.DATA, absolutePath)

            grantStoragePermission()
            mCameraTempUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraTempUri)
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun grantStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v("aaa", "Permission is granted")
                return true
            } else {

                Log.v("aaa", "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1);
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("aaa", "Permission is granted");
            return true
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
        val triple = myImageView.detectFace() ?: return null
        val pointFList: Array<PointF?> = triple.first
        val eyesDistances: Array<Float?> = triple.second
        val confidences: Array<Float?> = triple.third
        val newBitmaps: ArrayList<Bitmap> = ArrayList()
        for (i in pointFList.indices) {
            if (pointFList[i] != null && eyesDistances[i] != null && confidences[i]!! >= 0.51) {
                val pointF = pointFList[i]!!
                val eyesDistance = eyesDistances[i]!!
                val newBitmap: Bitmap = getCroppedBitmap(bitmap, pointF, eyesDistance)
                newBitmaps.add(newBitmap)
            }

        }

//        myImageView.setImgBitmap(newBitmaps[1])

        return newBitmaps
    }

    private fun getCroppedBitmap(bitmap: Bitmap, pointF: PointF, eyesDistance: Float): Bitmap {
        val scale = 1.8
        var x = 0
        var y = 0
        val size: Int
        if (pointF.x - eyesDistance * scale > 0) {
            x = (pointF.x - eyesDistance * scale).toInt()
        }
        if (pointF.y - eyesDistance * scale > 0) {
            y = (pointF.y - eyesDistance * scale).toInt()
        }
        if (x + 2 * eyesDistance * scale < bitmap.width && y + 2 * eyesDistance * scale < bitmap.height) {
            size = (2 * eyesDistance * scale).toInt()
        } else if (x > y) {
            size = bitmap.width - x
        } else {
            size = bitmap.height - y
        }

        return Bitmap.createBitmap(bitmap, x, y, size, size)

    }


    private fun predict(bitmaps: ArrayList<Bitmap>?) {

        if (bitmaps != null) {

            val imgSize = 64
            val agePredictionList = FloatArray(bitmaps.size)

            for (i in bitmaps.indices) {

                val ages = FloatArray(1)
                val resizedImage = ImageUtils.processBitmap(bitmaps[i], imgSize)
                inputValues = ImageUtils.normalizeBitmap(resizedImage, imgSize, 0f, 1.0f)

                //Pass  input  into  the  tensorflow
                tf!!.feed(inputName, inputValues, 1, imgSize.toLong(), imgSize.toLong(), 3)

                //compute  agePredictionList
                tf!!.run(arrayOf(outputName))

                //copy  the  output  into  the  agePredictionList  array
                tf!!.fetch(outputName, ages)

                agePredictionList[i] = ages[0]

            }

            myImageView.setDrawText(agePredictionList)

        }
    }
}