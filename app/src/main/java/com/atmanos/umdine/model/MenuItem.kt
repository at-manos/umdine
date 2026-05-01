package com.atmanos.umdine.model

/**
 * Anna Howell
 * Class for storing information about a specific menu item
 * and calculating the average rating
 */
class MenuItem {
    private var name : String = ""
    private var numRatings : Int = 0
    private var numStars : Float = 0.0f // can be half a star

    constructor(name : String, numRatings: Int, numStars : Float) {
        this.name = name
        this.numRatings = numRatings
        this.numStars = numStars
    }

    fun getName() : String {
        return name
    }

    fun getNumRatings() : Int {
        return numRatings
    }

    fun getNumStars() : Float {
        return numStars
    }

    fun setName(name : String) {
        this.name = name
    }

    fun setNumRatings(numRatings : Int) {
        this.numRatings = numRatings
    }

    fun setNumStars(numStars : Float) {
        this.numStars = numStars
    }

    fun changeRating(from : Float, to : Float) {
        numStars = (numStars - from) + to
    }

    fun getAvgRating() : Float {
        var avgRating : Float = 0.0f
        if (numRatings != 0) {
            avgRating = numStars / numRatings
        }
        return avgRating
    }

    fun addRating(stars : Float) {
        numRatings += 1
        numStars += stars
    }
}