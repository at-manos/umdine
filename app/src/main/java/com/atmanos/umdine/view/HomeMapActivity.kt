package com.atmanos.umdine.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atmanos.umdine.R
import com.atmanos.umdine.model.DiningHallId
import com.atmanos.umdine.model.Model

/**
 * Aiden Manos
 * TODO: Google Maps showing nearby dining halls
 * TODO: Busyness of each hall
 * TODO: Banner ad
 */
class HomeMapActivity : AppCompatActivity() {
    
    private lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_map)
        
        model = Model(this)
        model.getDiningHalls { halls ->
            // update UI
        }
        findViewById<Button>(R.id.btnGoToDiningHall).setOnClickListener {
            val intent = Intent(this, DiningHallActivity::class.java)
            startActivity(intent)
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        var hall : DiningHallId = DiningHallId.SOUTH_CAMPUS
        lateinit var pref : SharedPreferences
    }
}
