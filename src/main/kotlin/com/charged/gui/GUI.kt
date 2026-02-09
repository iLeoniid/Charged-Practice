package com.charged.gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory

abstract class GUI(
    protected val title: String,
    protected val size: Int
) {
    protected val inventory: Inventory = Bukkit.createInventory(null, size, title)
    
    abstract fun setup()
    
    open fun open(player: Player) {
        setup()
        player.openInventory(inventory)
    }
    
    abstract fun handleClick(event: InventoryClickEvent)
    
    protected fun cancel(event: InventoryClickEvent) {
        event.isCancelled = true
    }
}