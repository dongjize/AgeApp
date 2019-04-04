package com.example.ageapp.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.ageapp.R
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.InputStream
import java.lang.Exception

class PhotoActivity : AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        takePhoto.setOnClickListener(this)
        selectAlbum.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.takePhoto -> {
                val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePhotoIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePhotoIntent, 1)
                }
            }
            R.id.selectAlbum -> {
                val choosePhotoIntent = Intent()
                choosePhotoIntent.type = "image/*"
                choosePhotoIntent.action = Intent.ACTION_GET_CONTENT
                if (choosePhotoIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(Intent.createChooser(choosePhotoIntent, "select pic"), 2)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val picBitmap: Bitmap = data.extras.get("data") as Bitmap
                    ivPhoto.setImageBitmap(picBitmap)
                    showToast("take photo!")
                    MediaStore.Images.Media.insertImage(contentResolver, picBitmap, "title", "description")
                }
            }

            2 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri: Uri? = data.data
                    if (uri != null) {
                        var inStream: InputStream? = null
                        try {
                            inStream = contentResolver.openInputStream(uri)
                            val selPicBitmap: Bitmap = BitmapFactory.decodeStream(inStream)
                            ivPhoto.setImageBitmap(selPicBitmap)
                        } catch (e: Exception) {
                            showToast(e.message!!)
                        }
                    }
                }
            }
        }
    }
}