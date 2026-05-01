package com.atmanos.umdine.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atmanos.umdine.R

/**
 * Aiden Manos
 * TODO: Google Maps showing nearby dining halls
 * TODO: Busyness of each hall
 * TODO: Banner ad
 */
class HomeMapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //shared preferences
        pref = this.getSharedPreferences(this.packageName + "_preferences", Context.MODE_PRIVATE)
    }

    companion object {
        var hall : String = ""
        lateinit var pref : SharedPreferences
    }
}
