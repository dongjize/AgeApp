package com.example.ageapp.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.TextureView
import android.view.View
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}