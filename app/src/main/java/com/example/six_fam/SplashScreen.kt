package com.example.six_fam

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isUserLoggedIn = SharedPref.getBoolean(PrefConst.isUserLoggedIn)

        if (isUserLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}