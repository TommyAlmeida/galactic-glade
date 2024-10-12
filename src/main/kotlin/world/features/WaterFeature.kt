package io.poin.game.world.features

interface WaterFeature {
    fun shouldPlaceWater(x: Double, z: Double, height: Double): Boolean
    fun getWaterHeight(x: Double, z: Double): Double
}