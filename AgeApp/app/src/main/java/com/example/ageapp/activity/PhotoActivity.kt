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
import com.example.ageapp.*
import com.example.ageapp.util.ImageUtils
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.IOException


class PhotoActivity : AppCompatActivity(), View.OnClickListener {

    private var bitmap: Bitmap? = null
    private var filePath: String? = null


    private var imgUri: Uri? = null
    private var imageFile: File? = null
    private var imageCropFile: File? = null


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

        analyzeBtn.setOnClickListener(this)
        takePhoto.setOnClickListener(this)
        selectAlbum.setOnClickListener(this)

        tf = TensorFlowInferenceInterface(assets, ageModelPath)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.analyzeBtn -> {
                if (bitmap != null) {
                    predict(bitmap!!)
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
                    bitmap = rotateImageView(readPictureDegree(filePath!!), BitmapFactory.decodeStream(inStream, null, options))
                    photoView.setImageBitmap(bitmap)


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
                            bitmap = BitmapFactory.decodeStream(inStream, null, options)
                            photoView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            showToast(e.message!!)
                        }
                    }
                }
            }

//            REQUEST_CODE_CAPTURE_CROP -> {
//                imageCropFile?.let {
//                    bitmap = BitmapFactory.decodeFile(it.absolutePath)
//                    photoView.setImageBitmap(bitmap)
//                }
//            }
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
                ErrorDialog.newInstance(getString(R.string.request_permission)).show(supportFragmentManager, "dialog")
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


//    private fun gotoCaptureCrop() {
//        imageFile = FileUtil.createImageFile()
//
//        imageFile?.let {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                imgUri = FileProvider.getUriForFile(this, AUTHORITY, it)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
//            } else {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it))
//            }
//
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
//            intent.resolveActivity(packageManager)?.let {
//                startActivityForResult(intent, TAKE_PHOTO_REQUEST)
//            }
//        }
//    }


//    private fun gotoCrop(sourceUri: Uri) {
//        imageCropFile = FileUtil.createImageFile(true) //创建一个保存裁剪后照片的File
//        imageCropFile?.let {
//            val intent = Intent("com.android.camera.action.CROP")
//            intent.putExtra("crop", "true")
//            intent.putExtra("aspectX", 1)    //X方向上的比例
//            intent.putExtra("aspectY", 1)    //Y方向上的比例
//            intent.putExtra("outputX", 500)  //裁剪区的宽
//            intent.putExtra("outputY", 500) //裁剪区的高
//            intent.putExtra("scale ", true)  //是否保留比例
//            intent.putExtra("return-data", false) //是否在Intent中返回图片
//            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()) //设置输出图片的格式
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
//                intent.setDataAndType(sourceUri, "image/*")  //设置数据源,必须是由FileProvider创建的ContentUri
//
//                val imgCropUri = Uri.fromFile(it)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgCropUri) //设置输出  不需要ContentUri,否则失败
//                Log.d("tag", "input $sourceUri")
//                Log.d("tag", "output ${Uri.fromFile(it)}")
//            } else {
//                intent.setDataAndType(Uri.fromFile(imageFile!!), "image/*")
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it))
//            }
//            startActivityForResult(intent, REQUEST_CODE_CAPTURE_CROP)
//        }
//    }


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


    private fun predict(bitmap: Bitmap) {
        //Resize  the  image  into  224  x  224
        val resizedImage = ImageUtils.processBitmap(bitmap, 64)

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

//        try {

        val conf = (confidence * 100).toString().substring(0, 5)
        //Convert  predicted  class  index  into  actual  label  name
        showToast(age.toString().plus(" ").plus(conf).plus(" ").plus(gender))
        photoView.showAge()

//        } catch (e: Exception) {
//        }


    }


}