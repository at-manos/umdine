package com.atmanos.umdine.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Anna Howell
 * Class for storing information about a specific menu item
 * and calculating the average rating
 */
@IgnoreExtraProperties
data class MenuItem(
    var name: String = "",
    var numRatings: Int = 0,
    var numStars: Float = 0.0f,
    var dietaryTags: List<String> = emptyList()
) {
    fun changeRating(from: Float, to: Float) {
        numStars = (numStars - from) + to
    }

    fun getAvgRating(): Float = if (numRatings > 0) numStars / numRatings else 0.0f

    fun addRating(stars: Float) {
        numRatings += 1
        numStars += stars
    }

    fun hasTag(tag: String): Boolean = dietaryTags.any { it.equals(tag, ignoreCase = true) }
}
