package com.atmanos.umdine.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.Transaction
import com.google.firebase.database.MutableData
import androidx.core.content.edit

enum class DiningHallId(val id: String, val displayName: String) {
    YAHENTAMITSI("yahentamitsi", "Yahentamitsi"),
    SOUTH_CAMPUS("south_campus", "South Campus"),
    NORTH_251("251_north", "251 North");

    companion object {
        fun fromId(id: String): DiningHallId? = entries.find { it.id == id }
    }
}

/**
 * Aiden Manos
 * Data handler for Firebase and SharedPreferences
 */
class Model(context: Context) {

    private val database = FirebaseDatabase.getInstance()

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "${context.packageName}_preferences",
        Context.MODE_PRIVATE
    )

    /**
     * Subscribe to menu changes for a hall
     */
    fun observeMenu(hall: DiningHallId, listener: ValueEventListener) {
        database.getReference("dining_halls")
            .child(hall.id)
            .child("menu")
            .addValueEventListener(listener)
    }

    /**
     * Submit a rating for an item
     */
    fun submitRating(hall: DiningHallId, itemKey: String, userRating: Float) {
        val oldRating = getUserRating(hall, itemKey)
        val menuRef = database.getReference("dining_halls").child(hall.id).child("menu").child(itemKey)

        menuRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val item = currentData.getValue(MenuItem::class.java) ?: return Transaction.success(currentData)
                if (oldRating == -1.0f) {
                    item.addRating(userRating)
                } else {
                    item.changeRating(oldRating, userRating)
                }
                currentData.value = item
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    val prefKey = "${hall.id}_${itemKey}"
                    prefs.edit { putFloat(prefKey, userRating) }
                    Log.d("Model", "Rating for $itemKey updated successfully")
                } else {
                    Log.e("Model", "Transaction failed: ${error?.message}")
                }
            }
        })
    }

    /**
     * Get rating for an item, or -1 if not set.
     */
    fun getUserRating(hall: DiningHallId, itemKey: String): Float {
        val prefKey = "${hall.id}_${itemKey}"
        return prefs.getFloat(prefKey, -1.0f)
    }

    /**
     * Submit wait time for a hall
     */
    fun submitWaitTime(hall: DiningHallId, minutes: Int) {
        val hallRef = database.getReference("dining_halls").child(hall.id)

        val currentTime = System.currentTimeMillis()
        hallRef.child("wait_times").child(currentTime.toString()).setValue(minutes)
        hallRef.child("current_busyness").setValue(minutes)
    }

    /**
     * Get current wait times
     */
    fun getDiningHalls(callback: (List<DiningHall>) -> Unit) {
        database.getReference("dining_halls").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = snapshot.children.mapNotNull {
                    it.getValue(DiningHall::class.java)?.apply { id = it.key ?: "" }
                }
                callback(results)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Model", "Error from Firebase: ${error.message}")
            }
        })
    }

    /**
     * Save dietary restrictions
     */
    fun saveDietaryRestrictions(restrictions: Set<String>) {
        prefs.edit { putStringSet("restrictions", restrictions) }
    }

    /**
     * Get dietary restrictions
     */
    fun getDietaryRestrictions(): Set<String> {
        return prefs.getStringSet("restrictions", emptySet()) ?: emptySet()
    }

    /**
     * Toggle a dish as a favorite
     */
    fun toggleFavorite(dishId: String) {
        val favorites = getFavorites().toMutableSet()
        if (favorites.contains(dishId)) {
            favorites.remove(dishId)
        } else {
            favorites.add(dishId)
        }
        prefs.edit { putStringSet("favorites", favorites) }
    }
    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }
}

data class DiningHall(
    var id: String = "",
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var current_busyness: Int = 0,
    var menu: Map<String, MenuItem> = emptyMap()
)
