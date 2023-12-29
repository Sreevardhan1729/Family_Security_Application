package com.example.six_fam

import android.app.Application

class six_famApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPref.init(this)
    }
}