package io.poin.game.world.generator

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.pipeline.JNoise
import io.poin.game.world.features.NoiseFeature
import io.poin.game.world.selectors.BlockSelector
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import kotlin.math.max
import kotlin.random.Random

class MarsGenerator : AbstractTerrainGenerator() {
    private val baseNoise: NoiseFeature = NoiseFeature(
        JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator.newBuilder().build())
            .scale(0.001)
            .build(),
        scale = 128.0,
        offset = 64.0,
        name = "Base Terrain",
    )

    private val craterNoise: NoiseFeature = NoiseFeature(
        JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().build())
            .scale(0.01)
            .build(),
        scale = 20.0,
        name = "Crater"
    )

    private val roughnessNoise: NoiseFeature = NoiseFeature(
        JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().build())
            .scale(0.05)
            .build(),
        scale = 3.0,
        name = "Surface Roughness"
    )

    private val latitudeNoise: FastSimplexNoiseGenerator = FastSimplexNoiseGenerator.newBuilder().build()

    override val blockSelector: BlockSelector = object : BlockSelector {
        override fun selectBlock(height: Double, x: Double, z: Double): Block {
            return when {
                height < 60 -> Block.BEDROCK
                height < 65 -> Block.RED_SANDSTONE
                height < 128 -> Block.RED_SAND
                else -> Block.TERRACOTTA
            }
        }
    }

    init {

        addFeature(baseNoise)
        addFeature(craterNoise)
        addFeature(roughnessNoise)
    }

    override fun calculateHeight(x: Double, z: Double): Double {
        return baseNoise.apply(x, z) + roughnessNoise.apply(x, z)
    }

    override fun adjustHeight(x: Double, z: Double, height: Double): Double {
        val craterDepth = craterNoise.apply(x, z)
        return max(height - craterDepth.coerceAtLeast(0.0), 1.0) // Ensure minimum height of 1
    }

    override fun generateTerrain(unit: GenerationUnit, pos: Pos, height: Double, start: Point, end: Point) {
        super.generateTerrain(unit, pos, height, start, end)

        // Add exposed bedrock in deep craters
        if (craterNoise.apply(pos.x(), pos.z()) > 10) {
            val surfacePos = Pos(pos.x(), height, pos.z())
            if (isWithinBounds(surfacePos, start, end)) {
                unit.modifier().setBlock(surfacePos, Block.STONE)
            }
        }
    }

    override fun addFeatures(unit: GenerationUnit, pos: Pos, start: Point, end: Point) {
        if (Random.nextDouble() < 0.01) {
            when (Random.nextInt(3)) {
                0 -> addMushroom(unit, pos, start, end)
                1 -> addDeadBush(unit, pos, start, end)
                2 -> addRock(unit, pos, start, end)
            }
        }
    }

    private fun addMushroom(unit: GenerationUnit, pos: Pos, start: Point, end: Point) {
        val mushroomPos = pos.add(0.0, 1.0, 0.0)
        if (isWithinBounds(mushroomPos, start, end)) {
            unit.modifier().setBlock(mushroomPos, Block.RED_MUSHROOM_BLOCK)
        }
    }

    private fun addDeadBush(unit: GenerationUnit, pos: Pos, start: Point, end: Point) {
        val bushPos = pos.add(0.0, 1.0, 0.0)
        if (isWithinBounds(bushPos, start, end)) {
            unit.modifier().setBlock(bushPos, Block.DEAD_BUSH)
        }
    }

    private fun addRock(unit: GenerationUnit, pos: Pos, start: Point, end: Point) {
        val rockHeight = Random.nextInt(1, 4)
        for (y in 0 until rockHeight) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (Random.nextDouble() < 0.7) {
                        val rockPos = pos.add(x.toDouble(), y + 1.0, z.toDouble())
                        if (isWithinBounds(rockPos, start, end)) {
                            unit.modifier().setBlock(rockPos, Block.STONE)
                        }
                    }
                }
            }
        }
    }
}