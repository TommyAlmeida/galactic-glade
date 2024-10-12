package io.poin.game.world.features

import de.articdive.jnoise.pipeline.JNoise

class NoiseFeature(
    private val noiseGenerator: JNoise,
    private val scale: Double = 1.0,
    private val offset: Double = 0.0,
    private val name: String
) : TerrainFeature {
    override fun apply(x: Double, z: Double): Double {
        return noiseGenerator.evaluateNoise(x, z) * scale + offset
    }

    override fun getName(): String = name
}