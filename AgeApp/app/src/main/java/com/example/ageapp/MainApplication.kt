package com.example.ageapp

import android.app.Application

class MainApplication : Application() {

    private lateinit var app: MainApplication

    fun getInstance(): MainApplication {
        return app
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}