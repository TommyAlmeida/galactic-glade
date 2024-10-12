package io.poin.game.world.decoration

import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.generator.GenerationUnit
import kotlin.random.Random

interface Decoration {
    fun generate(unit: GenerationUnit, pos: Pos, random: Random)
}