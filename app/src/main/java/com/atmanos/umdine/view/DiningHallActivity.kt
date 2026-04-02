package com.atmanos.umdine.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atmanos.umdine.R

/**
 * NAME
 * TODO: Details about a specific dining hall
 * TODO: Menu with a RatingBar for each menu item (OnRatingBarChangeListener)
 * TODO: User ratings with a SeekBar for submitting wait time (OnSeekBarChangeListener)
 */
class DiningHallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dining_hall)
    }
}
