package io.poin.game.world.generator

import io.poin.game.world.features.TerrainFeature
import io.poin.game.world.selectors.BlockSelector
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.instance.generator.GenerationUnit
import net.minestom.server.instance.generator.Generator
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.max

abstract class AbstractTerrainGenerator : Generator {
    private val features = mutableListOf<TerrainFeature>()
    protected abstract val blockSelector: BlockSelector

    private val logger = Logger.getLogger(this.javaClass.name)

    private var heightMapLogged = false

    private var enableHeightMapLogging = true

    fun addFeature(feature: TerrainFeature) {
        features.add(feature)
    }

    override fun generate(unit: GenerationUnit) {
        val start: Point = unit.absoluteStart()
        val end: Point = unit.absoluteEnd()
        val heightMap = mutableMapOf<Pair<Int, Int>, Double>()

        // First pass: Generate height map
        for (x in 0 until unit.size().x().toInt()) {
            for (z in 0 until unit.size().z().toInt()) {
                val worldX = x + start.x()
                val worldZ = z + start.z()
                heightMap[Pair(x, z)] = calculateHeight(worldX, worldZ)
            }
        }

        // Log height maps for each feature (only once)
        if (enableHeightMapLogging && !heightMapLogged) {
            logHeightMaps(start, end)
            heightMapLogged = true
        }

        // Second pass: Generate terrain and features
        for (x in 0 until unit.size().x().toInt()) {
            for (z in 0 until unit.size().z().toInt()) {
                val worldX = x + start.x()
                val worldZ = z + start.z()
                var height = heightMap[Pair(x, z)] ?: continue
                height = adjustHeight(worldX, worldZ, height)
                generateTerrain(unit, Pos(worldX, height, worldZ), height, start, end)
                addFeatures(unit, Pos(worldX, height, worldZ), start, end)
            }
        }
    }

    protected abstract fun calculateHeight(x: Double, z: Double): Double
    protected abstract fun adjustHeight(x: Double, z: Double, height: Double): Double

    protected open fun generateTerrain(unit: GenerationUnit, pos: Pos, height: Double, start: Point, end: Point) {
        for (y in start.y().toInt()..height.toInt()) {
            val blockPos = Pos(pos.x(), y.toDouble(), pos.z())
            if (isWithinBounds(blockPos, start, end)) {
                val block = blockSelector.selectBlock(y.toDouble(), pos.x(), pos.z())
                unit.modifier().setBlock(blockPos, block)
            }
        }
    }

    protected abstract fun addFeatures(unit: GenerationUnit, pos: Pos, start: Point, end: Point)

    protected fun isWithinBounds(pos: Pos, start: Point, end: Point): Boolean {
        return pos.x() >= start.x() && pos.x() < end.x() &&
                pos.y() >= start.y() && pos.y() < end.y() &&
                pos.z() >= start.z() && pos.z() < end.z()
    }

    fun debugVisualize(player: Player, chunkX: Int, chunkZ: Int) {
        val chunkSize = 16
        for (x in 0 until chunkSize) {
            for (z in 0 until chunkSize) {
                val worldX = chunkX * chunkSize + x
                val worldZ = chunkZ * chunkSize + z
                val height = calculateHeight(worldX.toDouble(), worldZ.toDouble())
                features.forEach { _ ->
                    val packet = ParticlePacket(
                        Particle.DUST,
                        false,
                        worldX.toDouble(),
                        height,
                        worldZ.toDouble(),
                        255f,
                        0f,
                        0f,
                        1f,
                        1
                    )

                    player.sendPacket(packet)
                }
            }
        }
    }

    /**
     * Calculates a suitable spawn position.
     * @param searchRadius The radius to search for a spawn position
     * @param minHeight The minimum acceptable height for spawning
     * @param maxHeight The maximum acceptable height for spawning
     * @param maxSlope The maximum acceptable slope for spawning
     * @return A Pos representing the spawn position, or null if no suitable position is found
     */
    fun calculateSpawnPosition(searchRadius: Int, minHeight: Double, maxHeight: Double, maxSlope: Double): Pos? {
        val center = Pos(0.0, 0.0, 0.0)
        var bestPos: Pos? = null
        var lowestSlope = Double.MAX_VALUE

        for (x in -searchRadius..searchRadius) {
            for (z in -searchRadius..searchRadius) {
                val pos = center.add(x.toDouble(), 0.0, z.toDouble())
                val height = calculateHeight(pos.x(), pos.z())

                if (height < minHeight || height > maxHeight) continue

                val slope = calculateSlope(pos.x(), pos.z())
                if (slope <= maxSlope && slope < lowestSlope) {
                    bestPos = pos.withY(height)
                    lowestSlope = slope
                }
            }
        }

        return bestPos
    }

    /**
     * Calculates the slope at a given position.
     * @param x The x-coordinate
     * @param z The z-coordinate
     * @return The slope value
     */
    private fun calculateSlope(x: Double, z: Double): Double {
        val sampleDistance = 1.0

        val heightNorth = calculateHeight(x, z - sampleDistance)
        val heightSouth = calculateHeight(x, z + sampleDistance)
        val heightEast = calculateHeight(x + sampleDistance, z)
        val heightWest = calculateHeight(x - sampleDistance, z)

        val slopeNS = abs(heightNorth - heightSouth) / (2 * sampleDistance)
        val slopeEW = abs(heightEast - heightWest) / (2 * sampleDistance)

        return max(slopeNS, slopeEW)
    }

    private fun logHeightMaps(start: Point, end: Point) {
        val sampleSize = 10 // Number of sample points in each dimension
        val stepX = (end.x() - start.x()) / sampleSize
        val stepZ = (end.z() - start.z()) / sampleSize

        features.forEach { feature ->
            logger.info("Height map for ${feature.getName()}:")
            val heightMap = StringBuilder()
            for (x in 0 until sampleSize) {
                for (z in 0 until sampleSize) {
                    val worldX = start.x() + x * stepX
                    val worldZ = start.z() + z * stepZ
                    val height = feature.apply(worldX, worldZ)
                    heightMap.append("%4d ".format(height.toInt()))
                }
                heightMap.append("\n")
            }
            logger.info(heightMap.toString())
        }
    }
}