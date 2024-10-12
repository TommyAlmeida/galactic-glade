package io.poin.game.world.decoration

import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import kotlin.random.Random

abstract class TreeDecoration(
    protected val trunkBlock: Block,
    protected val leafBlock: Block
) : Decoration {
    abstract override fun generate(unit: GenerationUnit, pos: Pos, random: Random)
}