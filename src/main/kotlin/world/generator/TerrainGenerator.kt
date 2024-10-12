package io.poin.game.world.generator

import io.poin.game.world.decoration.Decoration
import io.poin.game.world.decoration.DecorationPlacer
import io.poin.game.world.features.TerrainFeature
import io.poin.game.world.selectors.BlockSelector
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.instance.generator.Generator

class TerrainGenerator(
    private val baseFeature: TerrainFeature,
    private val blockSelector: BlockSelector
) : Generator {

    private val features = mutableListOf<TerrainFeature>()
    private val decorationPlacers = mutableListOf<DecorationPlacer>()
    private var debugMode = DebugMode.NONE

    enum class DebugMode {
        NONE,
        HEIGHT_MAP,
        FEATURE_CONTRIBUTION
    }

    private var customGenerationMethod: ((GenerationUnit, Map<Pair<Int, Int>, Double>) -> Unit)? = null

    fun setGenerationMethod(method: (GenerationUnit, Map<Pair<Int, Int>, Double>) -> Unit) {
        customGenerationMethod = method
    }


    fun addFeature(feature: TerrainFeature) {
        features.add(feature)
    }

    fun addDecoration(decoration: Decoration, density: Double, minHeight: Double, maxHeight: Double) {
        decorationPlacers.add(DecorationPlacer(decoration, density, minHeight, maxHeight))
    }

    fun setDebugMode(mode: DebugMode) {
        debugMode = mode
    }

    override fun generate(unit: GenerationUnit) {
        val start: Point = unit.absoluteStart()
        val heightMap = mutableMapOf<Pair<Int, Int>, Double>()

        // First pass: Generate height map
        for (x in -1..unit.size().x().toInt()) {
            for (z in -1..unit.size().z().toInt()) {
                val worldX = x + start.x()
                val worldZ = z + start.z()
                heightMap[Pair(x, z)] = calculateHeight(worldX.toDouble(), worldZ.toDouble())
            }
        }

        // Second pass: Generate terrain
        if (customGenerationMethod != null) {
            customGenerationMethod?.invoke(unit, heightMap)
        } else {
            // Default generation method
            for (x in 0 until unit.size().x().toInt()) {
                for (z in 0 until unit.size().z().toInt()) {
                    val worldX = x + start.x()
                    val worldZ = z + start.z()
                    val height = heightMap[Pair(x, z)] ?: continue
                    val pos = Pos(worldX.toDouble(), height, worldZ.toDouble())

                    val block = blockSelector.selectBlock(height, worldX.toDouble(), worldZ.toDouble())
                    unit.modifier().setBlock(pos, block)
                }
            }
        }

        // Apply decorations
        if(debugMode == DebugMode.NONE) applyDecorations(unit, heightMap)
    }

    private fun calculateHeight(x: Double, z: Double): Double {
        var height = baseFeature.apply(x, z)
        features.forEach { height += it.apply(x, z) }
        return height
    }

    private fun getHeightMapBlock(height: Double): Block {
        return when {
            height < 60 -> Block.BLUE_CONCRETE
            height < 80 -> Block.GREEN_CONCRETE
            height < 100 -> Block.YELLOW_CONCRETE
            else -> Block.RED_CONCRETE
        }
    }

    private fun applyDecorations(unit: GenerationUnit, heightMap: Map<Pair<Int, Int>, Double>) {
        decorationPlacers.forEach { placer ->
            unit.fork {
                placer.place(unit, heightMap)
            }
        }
    }
}