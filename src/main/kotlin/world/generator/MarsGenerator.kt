package io.poin.game.world.generator

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.generator.Generator
import kotlin.random.Random
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction
import de.articdive.jnoise.pipeline.JNoise
import io.poin.game.world.features.NoiseFeature
import io.poin.game.world.selectors.BlockSelector
import io.poin.game.world.utils.AbsClampNoiseModifier
import net.minestom.server.coordinate.Point
import kotlin.math.abs

class MarsGenerator : Generator {
    val absModifier = AbsClampNoiseModifier()

    private val baseNoise: NoiseFeature = NoiseFeature(
        JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator.newBuilder().build())
            .scale(0.002)
            .octavate(5, 0.5, 2.2, FractalFunction.FBM, false)
            .addModifier(absModifier::apply)
            .build(),
        scale = 128.0,
        offset = 64.0,
        name = "Base Terrain"
    )

    private val craterNoise: NoiseFeature = NoiseFeature(
        JNoise.newBuilder()
            .fastSimplex(FastSimplexNoiseGenerator.newBuilder().build())
            .scale(0.01)
            .octavate(5, 0.3, 3.0, FractalFunction.FBM, false)
            .addModifier(absModifier::apply)
            .invert()
            .build(),
        scale = 20.0,
        name = "Crater"
    )

    private val roughnessNoise: NoiseFeature = NoiseFeature(
        JNoise.newBuilder()
            .perlin(PerlinNoiseGenerator.newBuilder().build())
            .scale(0.05)
            .build(),
        scale = 3.0,
        name = "Surface Roughness"
    )

    override fun generate(unit: GenerationUnit) {
        val start: Point = unit.absoluteStart()
        val heightMap = mutableMapOf<Pair<Int, Int>, Double>()

        // First pass: Generate height map
        for (x in -1..unit.size().x().toInt()) {
            for (z in -1..unit.size().z().toInt()) {
                val worldX = x + start.x()
                val worldZ = z + start.z()
                heightMap[Pair(x, z)] = calculateHeight(worldX, worldZ)
            }
        }

        // Second pass: Generate terrain and features
        for (x in 0 until unit.size().x().toInt()) {
            for (z in 0 until unit.size().z().toInt()) {
                val worldX = x + start.x()
                val worldZ = z + start.z()
                var height = heightMap[Pair(x, z)] ?: continue

                // Apply crater effect
                val craterDepth = craterNoise.apply(worldX, worldZ)
                height -= craterDepth.coerceAtLeast(0.0)

                // Fill from bedrock to surface
                for (y in 0..height.toInt()) {
                    val pos = Pos(worldX, y.toDouble(), worldZ)
                    val block = when {
                        y == 0 -> Block.BEDROCK
                        y < height - 5 -> Block.RED_SANDSTONE
                        y < height -> Block.RED_SAND
                        y == height.toInt() && craterDepth > 10 -> Block.STONE // Exposed bedrock in deep craters
                        else -> Block.RED_SAND
                    }
                    unit.modifier().setBlock(pos, block)
                }

            }
        }
    }

    private fun calculateHeight(x: Double, z: Double): Double {
        return baseNoise.apply(x, z) + roughnessNoise.apply(x, z)
    }

    private fun addRock(unit: GenerationUnit, pos: Pos) {
        val rockHeight = Random.nextInt(1, 4)
        for (y in 0 until rockHeight) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (Random.nextDouble() < 0.7) {
                        unit.modifier().setBlock(pos.add(x.toDouble(), y.toDouble(), z.toDouble()), Block.STONE)
                    }
                }
            }
        }
    }

    private fun addDustStorm(unit: GenerationUnit, pos: Pos) {
        val stormHeight = Random.nextInt(5, 15)
        for (y in 0 until stormHeight) {
            if (Random.nextDouble() < 0.3) {
                unit.modifier().setBlock(pos.add(0.0, y.toDouble(), 0.0), Block.BROWN_STAINED_GLASS_PANE)
            }
        }
    }
}