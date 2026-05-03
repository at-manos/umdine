package com.atmanos.umdine.view

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.atmanos.umdine.R
import com.atmanos.umdine.model.Model
import com.google.android.material.chip.Chip

class PreferencesActivity : AppCompatActivity() {

    private lateinit var model: Model
    private lateinit var favoritesContainer: LinearLayout
    private lateinit var emptyFavoritesText: TextView

    private val chipMap = mapOf(
        R.id.chipVegan to "Vegan",
        R.id.chipVegetarian to "Vegetarian",
        R.id.chipGlutenFree to "Gluten-Free",
        R.id.chipHalal to "Halal",
        R.id.chipDairyFree to "Dairy-Free",
        R.id.chipNutFree to "Nut-Free"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        model = Model(this)
        favoritesContainer = findViewById(R.id.favoritesContainer)
        emptyFavoritesText = findViewById(R.id.emptyFavoritesText)

        loadDietaryRestrictions()
        setupChipListeners()
        loadFavorites()
    }

    private fun loadDietaryRestrictions() {
        val saved = model.getDietaryRestrictions()
        for ((id, name) in chipMap) {
            findViewById<Chip>(id)?.isChecked = saved.contains(name)
        }
    }

    private fun setupChipListeners() {
        for ((id, _) in chipMap) {
            findViewById<Chip>(id)?.setOnCheckedChangeListener { _, _ -> saveCurrentRestrictions() }
        }
    }

    private fun saveCurrentRestrictions() {
        val selected = chipMap.entries
            .filter { (id, _) -> findViewById<Chip>(id)?.isChecked == true }
            .map { (_, name) -> name }
            .toSet()
        model.saveDietaryRestrictions(selected)
    }

    private fun loadFavorites() {
        val favorites = model.getFavorites()
        if (favorites.isEmpty()) {
            emptyFavoritesText.visibility = View.VISIBLE
            return
        }

        model.getDiningHalls { halls ->
            val items = mutableListOf<Triple<String, String, String>>()
            for (hall in halls) {
                for ((key, item) in hall.menu) {
                    val dishId = "${hall.id}_${key}"
                    if (favorites.contains(dishId)) {
                        items.add(Triple(item.name, hall.name, dishId))
                    }
                }
            }
            if (items.isEmpty()) {
                emptyFavoritesText.visibility = View.VISIBLE
            } else {
                for ((name, hallName, dishId) in items) {
                    addFavoriteRow(name, hallName, dishId)
                }
            }
        }
    }

    private fun addFavoriteRow(itemName: String, hallName: String, dishId: String) {
        val row = layoutInflater.inflate(R.layout.item_favorite, favoritesContainer, false)
        row.findViewById<TextView>(R.id.favItemName).text = itemName
        row.findViewById<TextView>(R.id.favHallName).text = hallName
        row.findViewById<ImageButton>(R.id.unfavoriteButton).setOnClickListener {
            model.toggleFavorite(dishId)
            favoritesContainer.removeView(row)
            if (favoritesContainer.childCount == 0) {
                emptyFavoritesText.visibility = View.VISIBLE
            }
        }
        favoritesContainer.addView(row)
    }
}
