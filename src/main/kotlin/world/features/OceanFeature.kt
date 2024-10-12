package io.poin.game.world.features

class OceanFeature(private val seaLevel: Double) : WaterFeature {
    override fun shouldPlaceWater(x: Double, z: Double, height: Double): Boolean {
        return height < seaLevel
    }

    override fun getWaterHeight(x: Double, z: Double): Double {
        return seaLevel
    }
}
