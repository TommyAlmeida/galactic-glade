package io.poin.game.commands

import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle

class ChunkCommand : Command("chunk") {
    init {
        setDefaultExecutor { sender, _ ->
            if (sender is Player) {
                val chunk = sender.chunk
                val position = sender.position

                if (chunk != null) {
                    sender.sendMessage(
                        """
                                Chunk Information:
                                Chunk: ${chunk.chunkX}, ${chunk.chunkZ}
                                Block: ${position.blockX()}, ${position.blockY()}, ${position.blockZ()}
                                Exact: ${position.x()}, ${position.y()}, ${position.z()}
                                """.trimIndent()
                    )

                    highlightChunk(sender)
                }
            } else {
                sender.sendMessage("This command can only be executed by a player.")
            }
        }
    }

    private fun highlightChunk(player: Player) {
        val chunk = player.chunk
        val instance = player.instance
        val chunkX = chunk!!.chunkX * 16
        val chunkZ = chunk.chunkZ * 16

        /*
         * Spawn particles along the edges of the chunk
         */
        for (i in 0..16) {
            spawnParticle(player, Vec(chunkX + i.toDouble(), player.position.y(), chunkZ.toDouble()))
            spawnParticle(player, Vec(chunkX + i.toDouble(), player.position.y(), (chunkZ + 16).toDouble()))
            spawnParticle(player, Vec(chunkX.toDouble(), player.position.y(), chunkZ + i.toDouble()))
            spawnParticle(player, Vec((chunkX + 16).toDouble(), player.position.y(), chunkZ + i.toDouble()))
        }

        /*
         * Spawn particles at the chunk corners from bottom to top
         */
        for (y in 0..255 step 5) {
            spawnParticle(player, Vec(chunkX.toDouble(), y.toDouble(), chunkZ.toDouble()))
            spawnParticle(player, Vec((chunkX + 16).toDouble(), y.toDouble(), chunkZ.toDouble()))
            spawnParticle(player, Vec(chunkX.toDouble(), y.toDouble(), (chunkZ + 16).toDouble()))
            spawnParticle(player, Vec((chunkX + 16).toDouble(), y.toDouble(), (chunkZ + 16).toDouble()))
        }
    }

    private fun spawnParticle(player: Player, position: Vec) {
        val particlePacket = ParticlePacket(
            Particle.DUST,
            true,
            position.x(), position.y(), position.z(),
            255f, 0f, 0f,  // offset
            0f,  // particle data
            100  // particle count
        )

        player.sendPacket(particlePacket)
    }
}