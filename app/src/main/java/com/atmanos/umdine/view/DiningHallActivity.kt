package com.atmanos.umdine.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import com.atmanos.umdine.R
import com.atmanos.umdine.model.DiningHall
import com.atmanos.umdine.model.MenuItem
import com.atmanos.umdine.model.Model
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

/**
 * Anna Howell
 * TODO: Details about a specific dining hall
 * TODO: Menu with a RatingBar for each menu item (OnRatingBarChangeListener)
 * TODO: User ratings with a SeekBar for submitting wait time (OnSeekBarChangeListener)
 *
 */
class DiningHallActivity : BaseActivity() {
    private lateinit var menuLayout : LinearLayout
    private lateinit var waitTimeText : TextView
    private lateinit var waitTimeButton : FloatingActionButton
    private lateinit var model: Model

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dining_hall)

        // get menu for specific dining hall that the user clicked on

        // display current rating information (stored in firebase)

        // also need a rating bar for users to click on and submit their rating
        // for each menu item
        // probably a plus sign next to each item?
        // floating action button

        model = Model(this)
        
        menuLayout = findViewById<LinearLayout>(R.id.menuItems)
        findViewById<TextView>(R.id.hallName).text = HomeMapActivity.hall.displayName
        waitTimeText = findViewById<TextView>(R.id.waitTimeText)
        waitTimeButton = findViewById<FloatingActionButton>(R.id.waitTimeButton)

        // get current wait time
        updateWaitTime()

        waitTimeButton.setOnClickListener { showWaitTimeScreen() }
        val backButton: FloatingActionButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        model.observeMenu(HomeMapActivity.hall, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    menuLayout.removeAllViews()
                    createMenuItems(snapshot)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("DiningHallActivity", "error: " + error.message)
            }
        })
    }

    fun createMenuItems(snapshot : DataSnapshot) {
        for (item in snapshot.children) {
            // make object
            val itemKey : String? = item.key
            val menuItem : MenuItem? = item.getValue(MenuItem::class.java)

            if (itemKey != null && menuItem != null) {
                val row = layoutInflater.inflate(R.layout.menu_item, menuLayout, false)
                val itemNameTV : TextView = row.findViewById<TextView>(R.id.itemName)
                val itemRatingBar : RatingBar = row.findViewById<RatingBar>(R.id.itemRating)
                val rateButton : FloatingActionButton = row.findViewById<FloatingActionButton>(R.id.addRating)

                itemNameTV.text = menuItem.name
                itemRatingBar.rating = menuItem.getAvgRating()

                val favoriteButton: ImageButton = row.findViewById(R.id.favoriteButton)
                val dishId = "${HomeMapActivity.hall.id}_${itemKey}"
                favoriteButton.setImageResource(
                    if (model.getFavorites().contains(dishId)) R.drawable.ic_favorite
                    else R.drawable.ic_favorite_border
                )
                favoriteButton.setOnClickListener {
                    model.toggleFavorite(dishId)
                    favoriteButton.setImageResource(
                        if (model.getFavorites().contains(dishId)) R.drawable.ic_favorite
                        else R.drawable.ic_favorite_border
                    )
                }

                rateButton.setOnClickListener { showRatingScreen(menuItem, itemKey) }

                menuLayout.addView(row)
            }
        }
    }

    fun showRatingScreen(item : MenuItem, itemKey : String) {
        val alert : AlertDialog.Builder = AlertDialog.Builder(this)
        val itemRatingLayout = layoutInflater.inflate(R.layout.item_rating, null)
        val addRatingBar : RatingBar = itemRatingLayout.findViewById<RatingBar>(R.id.addRatingBar)
        val addRatingTV : TextView = itemRatingLayout.findViewById<TextView>(R.id.addRatingText)

        // check if rating already exists
        var prefRating = model.getUserRating(HomeMapActivity.hall, itemKey)
        // if rating doesn't exist, text should say add rating for item
        if (prefRating == -1.0f) {
            addRatingTV.text = "Add rating for " + item.name
        } else {
            addRatingTV.text = "Change rating for " + item.name
            addRatingBar.rating = prefRating
        }

        val ratingListener = RatingDialog(itemKey, addRatingBar)
        alert.setView(itemRatingLayout)
        alert.setPositiveButton("Submit", ratingListener)
        alert.setNegativeButton("Cancel", ratingListener)
        alert.show()
    }

    @SuppressLint("MissingInflatedId")
    fun showWaitTimeScreen() {
        val alert : AlertDialog.Builder = AlertDialog.Builder(this)
        val waitTimeLayout = layoutInflater.inflate(R.layout.wait_time, null)
        val addWaitTimeTV : TextView = waitTimeLayout.findViewById<TextView>(R.id.addWaitTimeText)
        val slider : Slider = waitTimeLayout.findViewById<Slider>(R.id.slider)
        val sliderStatusTV : TextView = waitTimeLayout.findViewById<TextView>(R.id.sliderStatus)

        addWaitTimeTV.text = "Submit the current wait time for " + HomeMapActivity.hall.displayName
        slider.addOnChangeListener { _, value, _ ->
            sliderStatusTV.text = value.toInt().toString() + " min."
        }

        val waitTimeListener : WaitTimeDialog = WaitTimeDialog(slider)
        alert.setView(waitTimeLayout)
        alert.setPositiveButton("Submit", waitTimeListener)
        alert.setNegativeButton("Cancel", waitTimeListener)
        alert.show()
    }

    fun updateWaitTime() {
        model.getDiningHalls{ diningHalls ->
            var diningHall : DiningHall? = diningHalls.find { it.id == HomeMapActivity.hall.id }
            if (diningHall != null) {
                waitTimeText.text = diningHall.current_busyness.toString() + " min."
            }
        }
    }

    inner class RatingDialog(var itemKey : String, var addRatingBar : RatingBar) : DialogInterface.OnClickListener {
        override fun onClick(p0: DialogInterface?, p1: Int) {
            if (p1 == -1) {
                // submit rating
                val stars : Float = addRatingBar.rating
                model.submitRating(HomeMapActivity.hall, itemKey, stars)
            } else if (p1 == -2) {
                p0!!.dismiss()
            }
        }
    }

    inner class WaitTimeDialog(var slider : Slider) : DialogInterface.OnClickListener {
        override fun onClick(p0: DialogInterface?, p1: Int) {
            if (p1 == -1) {
                // submit rating
                model.submitWaitTime(HomeMapActivity.hall, slider.value.toInt())
                updateWaitTime()
            } else if (p1 == -2) {
                p0!!.dismiss()
            }
        }

    }
}
