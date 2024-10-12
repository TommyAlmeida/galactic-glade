package io.poin.game.world

import net.minestom.server.instance.block.Block
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.min

data class SlopeBlock(val slopeDegree: Double, val blockType: Block)

class TerrainInterpolator {
    companion object {
        private val SURFACE_SLOPE_BLOCKS = listOf(
            SlopeBlock(20.0, Block.GRASS_BLOCK),
            SlopeBlock(45.0, Block.MOSS_BLOCK),
            SlopeBlock(75.0, Block.MOSSY_COBBLESTONE),
            SlopeBlock(80.0, Block.COBBLESTONE),
            SlopeBlock(85.0, Block.STONE)
        )

        private val WATER_SLOPE_BLOCKS = listOf(
            SlopeBlock(45.0, Block.GRAVEL),
            SlopeBlock(75.0, Block.STONE),
            SlopeBlock(Double.MAX_VALUE, Block.STONE)
        )

        private val BEACH_SLOPE_BLOCKS = listOf(
            SlopeBlock(60.0, Block.SAND),
            SlopeBlock(90.0, Block.MOSS_BLOCK),
            SlopeBlock(Double.MAX_VALUE, Block.SAND)
        )

        fun smoothstep(edge0: Double, edge1: Double, x: Double): Double {
            val t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0)
            return t * t * (3 - 2 * t)
        }

        fun clamp(x: Double, min: Double, max: Double): Double {
            return max(min, min(max, x))
        }

        fun getRiverDepth(riverValue: Double, riverThreshold: Double, maxRiverDepth: Double): Double {
            val absRiverValue = abs(riverValue)
            return if (absRiverValue < riverThreshold) {
                val t = smoothstep(0.0, riverThreshold, absRiverValue)
                maxRiverDepth * (1 - t)
            } else {
                0.0
            }
        }

        fun calculateSlope(height: Double, neighborHeights: List<Double>): Double {
            val maxDifference = neighborHeights.maxOfOrNull { abs(it - height) } ?: 0.0
            return toDegrees(atan(maxDifference))
        }

        fun getBlockForSlope(height: Double, slope: Double): Block {
            return when {
                height >= 68 -> SURFACE_SLOPE_BLOCKS.first { slope <= it.slopeDegree }.blockType
                height > 61 -> BEACH_SLOPE_BLOCKS.first { slope <= it.slopeDegree }.blockType
                else -> WATER_SLOPE_BLOCKS.first { slope <= it.slopeDegree }.blockType
            }
        }

        fun applyContinentalness(baseHeight: Double, continentalness: Double): Double {
            return baseHeight + (continentalness * 40) // Adjust the multiplier to control continental influence
        }
    }
}
