package com.example.sumayerestaurant

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sumayerestaurant.ui.customer.PublicHomeActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch PublicHomeActivity (public restaurant home)
        startActivity(Intent(this, PublicHomeActivity::class.java))
        finish()
    }
}
