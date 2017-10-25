package com.brianroper.putitdown.views

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

import com.brianroper.putitdown.R

class SplashActivity : AppCompatActivity() {

    internal var SPLASH_DURATION = 1300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(R.transition.fade_in, R.transition.fade_out)
            finish()
        }, SPLASH_DURATION.toLong())
    }
}
