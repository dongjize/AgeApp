package com.example.ageapp.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.ageapp.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        realTime.setOnClickListener(this)
        takePhoto.setOnClickListener(this)
        choosePhoto.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.realTime -> {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
            R.id.takePhoto -> {
                showToast("take photo")
            }
            R.id.choosePhoto -> {
                showToast("choose from album")
            }
        }
    }

}
