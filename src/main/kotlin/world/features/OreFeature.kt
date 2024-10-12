package io.poin.game.world.features

import net.minestom.server.instance.block.Block

interface OreFeature {
    fun shouldPlaceOre(x: Double, y: Double, z: Double): Boolean
    fun getOreBlock(): Block
}
