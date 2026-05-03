package com.atmanos.umdine.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atmanos.umdine.R
import com.atmanos.umdine.model.Model

/**
 * NAME
 * TODO: Favorite dishes
 * TODO: Dietary restrictions
 */
class PreferencesActivity : BaseActivity() {
    
    private lateinit var model: Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
    }
}
