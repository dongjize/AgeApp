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
import com.example.ageapp.R
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.InputStream
import java.lang.Exception
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.support.v4.content.ContextCompat
import com.example.ageapp.REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION
import com.example.ageapp.SELECT_ALBUM_REQUEST
import com.example.ageapp.TAKE_PHOTO_REQUEST
import com.example.ageapp.dialog.ConfirmationDialog
import com.example.android.camera2basic.ErrorDialog
import java.io.File
import java.io.FileInputStream


class PhotoActivity : AppCompatActivity(), View.OnClickListener {

    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        takePhoto.setOnClickListener(this)
        selectAlbum.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
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
                    val picBitmap: Bitmap = BitmapFactory.decodeStream(inStream)
                    ivPhoto.setImageBitmap(picBitmap)

                    val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        requestWriteStoragePermission()
                        return
                    }
                    // store the photo
                    MediaStore.Images.Media.insertImage(contentResolver, picBitmap, "title", "description")

                }
            }

            SELECT_ALBUM_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri: Uri? = data.data
                    if (uri != null) {
                        val inStream: InputStream?
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

}