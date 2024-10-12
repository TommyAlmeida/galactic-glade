package io.poin.game

import io.poin.game.world.generator.MarsGenerator
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.entity.EntitySpawnEvent
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.ClickType


fun main() {
    val minecraftServer = MinecraftServer.init()
    val instanceManager = MinecraftServer.getInstanceManager()

    val instanceContainer = instanceManager.createInstanceContainer()


    instanceContainer.setChunkSupplier(::LightingChunk)
    println("- Light added")
    instanceContainer.setGenerator(MarsGenerator())
    println("- Generator added")
    instanceContainer.timeRate = 0;
    println("- Time rate set to 0")

    val globalEventHandler = MinecraftServer.getGlobalEventHandler();

    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player: Player = event.player
        event.spawningInstance = instanceContainer

        player.respawnPoint = Pos(-13.0, 120.0, 48.0)
        println("Player spawn point set to: ${player.respawnPoint}")
    }

    globalEventHandler.addListener(
        EntitySpawnEvent::class.java
    ) { event: EntitySpawnEvent ->
        if (event.entity !is Player) {
            return@addListener
        }

        val player: Player = event.entity as Player

        player.setGameMode(GameMode.CREATIVE)
        player.permissionLevel = 4
    }

    val inventory = Inventory(InventoryType.CHEST_6_ROW, Component.text("Container"))

    globalEventHandler.addListener(
        InventoryPreClickEvent::class.java
    ) { event: InventoryPreClickEvent ->
        val player = event.player
        val playerInventory = player.inventory

        println(event.clickType)
        println(event.slot)
        println(event.clickedItem)

        if (event.clickType == ClickType.RIGHT_CLICK) {
            val item = event.clickedItem
            if (event.inventory == null) {
                playerInventory.setItemStack(event.slot, item.withAmount(item.amount() * 2))
            } else {
                event.inventory!!.setItemStack(event.slot, item.withAmount(item.amount() * 2))
            }

            event.isCancelled = true
        }

        inventory.clear()

        if (player.openInventory !== inventory) {
            player.openInventory(inventory)
        }
    }

    // Register Events (set spawn instance, teleport player at spawn)
    // Start the server
    minecraftServer.start("0.0.0.0", 25565)
    println("Glade server started...")
}