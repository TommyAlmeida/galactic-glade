package io.poin.game.world.features

import net.minestom.server.instance.Instance

interface TerrainFeature {
    fun apply(x: Double, z: Double): Double
    fun getName(): String
    fun debugVisualize(instance: Instance, x: Double, y: Double, z: Double)
}
