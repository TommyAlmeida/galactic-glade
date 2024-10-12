package io.poin.game.world.features

interface TerrainFeature {
    fun apply(x: Double, z: Double): Double
    fun getName(): String
}
