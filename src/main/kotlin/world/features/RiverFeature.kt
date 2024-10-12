package io.poin.game.world.features

import de.articdive.jnoise.pipeline.JNoise
import kotlin.math.abs

class RiverFeature(
    private val riverNoiseGenerator: JNoise,
    private val riverWidth: Double,
    private val riverDepth: Double
) : WaterFeature {
    override fun shouldPlaceWater(x: Double, z: Double, height: Double): Boolean {
        val riverValue = abs(riverNoiseGenerator.evaluateNoise(x, z))
        return riverValue < riverWidth && height < getWaterHeight(x, z)
    }

    override fun getWaterHeight(x: Double, z: Double): Double {
        return 62.0 // Default water level, can be adjusted
    }
}