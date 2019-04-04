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
        usePhoto.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.realTime -> {
                val intent = Intent(this, RealTimeActivity::class.java)
                startActivity(intent)
            }
            R.id.usePhoto -> {
                val intent = Intent(this, PhotoActivity::class.java)
                startActivity(intent)
            }
        }
    }

}
