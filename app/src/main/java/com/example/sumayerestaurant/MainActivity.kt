package com.example.sumayerestaurant

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumayerestaurant.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
