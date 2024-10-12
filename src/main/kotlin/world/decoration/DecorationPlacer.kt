package io.poin.game.world.decoration

import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.generator.GenerationUnit
import kotlin.random.Random

class DecorationPlacer(
    private val decoration: Decoration,
    private val density: Double,
    private val minHeight: Double,
    private val maxHeight: Double
) {
    fun place(unit: GenerationUnit, heightMap: Map<Pair<Int, Int>, Double>) {
        val random = Random(unit.absoluteStart().blockX() * 31 + unit.absoluteStart().blockZ())
        for (x in 0 until unit.size().x().toInt()) {
            for (z in 0 until unit.size().z().toInt()) {
                val worldX = x + unit.absoluteStart().blockX().toDouble()
                val worldZ = z + unit.absoluteStart().blockZ().toDouble()
                val y = heightMap[Pair(x, z)] ?: continue
                if (y in minHeight..maxHeight && random.nextDouble() < density) {
                    decoration.generate(unit, Pos(worldX, y, worldZ), random)
                }
            }
        }
    }
}
