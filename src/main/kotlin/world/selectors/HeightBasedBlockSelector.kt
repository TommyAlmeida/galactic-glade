package io.poin.game.world.selectors

import net.minestom.server.instance.block.Block

class HeightBasedBlockSelector(private val heightRanges: List<Pair<ClosedRange<Double>, Block>>) : BlockSelector {
    override fun selectBlock(height: Double, x: Double, z: Double): Block {
        return heightRanges.firstOrNull { (range, _) -> height in range }?.second ?: Block.STONE
    }
}