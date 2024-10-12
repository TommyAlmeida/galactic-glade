package io.poin.game.world.features

import de.articdive.jnoise.pipeline.JNoise
import net.minestom.server.instance.Instance
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle

class NoiseFeature(
    private val noiseGenerator: JNoise,
    private val scale: Double = 1.0,
    private val offset: Double = 0.0,
    private val name: String,
) : TerrainFeature {
    override fun apply(x: Double, z: Double): Double {
        return noiseGenerator.evaluateNoise(x, z) * scale + offset
    }

    override fun getName(): String = name

    override fun debugVisualize(instance: Instance, x: Double, y: Double, z: Double) {
        val packet = ParticlePacket(
            Particle.DUST,
            false,
            x,
            y,
            z,
            255f,
            0f,
            0f,
            1f,
            1
        )
        instance.players.forEach { it.playerConnection.sendPacket(packet) }
    }
}