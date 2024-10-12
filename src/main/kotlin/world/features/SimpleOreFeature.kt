package io.poin.game.world.features

import net.minestom.server.instance.block.Block
import kotlin.random.Random

class SimpleOreFeature(
    private val oreBlock: Block,
    private val rarity: Double,
    private val minHeight: Double,
    private val maxHeight: Double
) : OreFeature {
    override fun shouldPlaceOre(x: Double, y: Double, z: Double): Boolean {
        return y in minHeight..maxHeight && Random.nextDouble() < rarity
    }

    override fun getOreBlock(): Block = oreBlock
}
