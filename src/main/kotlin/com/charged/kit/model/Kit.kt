package com.charged.kit.model

import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect

data class Kit(
    val name: String,
    val displayName: String,
    val armor: Array<ItemStack?>,
    val inventory: Map<Int, ItemStack>,
    val effects: List<PotionEffect> = emptyList(),
    val naturalRegeneration: Boolean = false,
    val hungerEnabled: Boolean = false
)