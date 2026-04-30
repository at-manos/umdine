package com.atmanos.umdine.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atmanos.umdine.R
import com.atmanos.umdine.model.MenuItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Anna Howell
 * TODO: Details about a specific dining hall
 * TODO: Menu with a RatingBar for each menu item (OnRatingBarChangeListener)
 * TODO: User ratings with a SeekBar for submitting wait time (OnSeekBarChangeListener)
 */
class DiningHallActivity : AppCompatActivity() {
    private lateinit var menuLayout : LinearLayout
    private lateinit var menuReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dining_hall)

        // get menu for specific dining hall that the user clicked on

        // display current rating information (stored in firebase)

        // also need a rating bar for users to click on and submit their rating
        // for each menu item
        // probably a plus sign next to each item?
        // floating action button

        // not sure if we want users to be able to rate individual dining halls
        menuLayout = findViewById<LinearLayout>(R.id.body)

        // firebase stuff
        var firebase : FirebaseDatabase = FirebaseDatabase.getInstance()
        menuReference = firebase.getReference("dining_halls").child(HomeMapActivity.hall).child("menu")
        var mdh : MenuDataHandler = MenuDataHandler()
        menuReference.addValueEventListener(mdh)
    }

    fun createMenuItems(snapshot : DataSnapshot) {
        for (item in snapshot.children) {
            // make object
            val itemKey : String = item.key
            val menuItem : MenuItem = item.getValue(MenuItem::class.java)

            val row = layoutInflater.inflate(R.layout.menu_item, menuLayout, false)
            val itemNameTV : TextView = row.findViewById<TextView>(R.id.itemName)
            val itemRatingBar : RatingBar = row.findViewById<RatingBar>(R.id.itemRating)
            val rateButton : Button = row.findViewById<Button>(R.id.addRating)

            itemNameTV.text = menuItem.getName()
            itemRatingBar.rating = menuItem.getAvgRating()
                // code to add rating
            rateButton.setOnClickListener { showRatingScreen(menuItem, itemKey) }

            menuLayout.addView(row)
        }
    }

    fun showRatingScreen(item : MenuItem, itemKey : String) {
        var alert : AlertDialog.Builder = AlertDialog.Builder(this)
        var itemRatingLayout = layoutInflater.inflate(R.layout.item_rating, null, true)
        var addRatingBar : RatingBar = itemRatingLayout.findViewById<RatingBar>(R.id.addRatingBar)
        var addRatingTV : TextView = itemRatingLayout.findViewById<TextView>(R.id.addRatingText)

        // check if rating already exists
        var prefKey : String = HomeMapActivity.hall + "_" + itemKey
        var prefRating : Float = HomeMapActivity.pref.getFloat(prefKey, -1.0f)
        // if rating doesn't exist, text should say add rating for item
        if (prefRating == -1.0f) {
            addRatingTV.text = "Add rating for " + item.getName()
        } else {
            addRatingTV.text = "Change rating for " + item.getName()
            addRatingBar.rating = prefRating
        }

        var ratingListener : RatingDialog = RatingDialog(itemKey, addRatingBar, item, prefRating)
        alert.setPositiveButton("Submit", ratingListener)
        alert.setNegativeButton("Cancel", ratingListener)
        alert.show()
    }

    inner class MenuDataHandler : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value != null) {
                menuLayout.removeAllViews()
                createMenuItems(snapshot)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("MainActivity", "error: " + error.message)
        }
    }

    inner class RatingDialog(var itemKey : String, var addRatingBar : RatingBar, var item : MenuItem, var prefRating : Float) : DialogInterface.OnClickListener {
        override fun onClick(p0: DialogInterface?, p1: Int) {
            if (p1 == -1) {
                // submit rating
                var stars : Float = addRatingBar.rating
                var itemReference = menuReference.child(itemKey)

                // if already rated, need to subtract old and add new
                if (prefRating == -1.0f) {
                    item.addRating(stars)
                }
                else {
                    item.changeRating(prefRating, stars)
                }
                itemReference.setValue(item)
                var prefKey : String = HomeMapActivity.hall + "_" + itemKey
                var editor : SharedPreferences.Editor = HomeMapActivity.pref.edit()
                editor.putFloat(prefKey, stars)
                editor.commit()
            } else if (p1 == -2) {
                p0!!.dismiss()
            }
        }
    }

}
