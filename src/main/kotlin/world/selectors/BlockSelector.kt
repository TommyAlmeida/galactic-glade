package io.poin.game.world.selectors

import net.minestom.server.instance.block.Block

interface BlockSelector {
    fun selectBlock(height: Double, x: Double, z: Double): Block
}
