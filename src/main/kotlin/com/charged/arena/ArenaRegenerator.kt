package com.charged.arena

import com.charged.Charged
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import java.io.*
import java.util.concurrent.CompletableFuture
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class ArenaRegenerator(private val plugin: Charged) {
    
    private val arenaSnapshots = mutableMapOf<String, ArenaSnapshot>()
    
    data class ArenaSnapshot(
        val blocks: Map<BlockPosition, BlockData>,
        val minX: Int,
        val minY: Int,
        val minZ: Int,
        val maxX: Int,
        val maxY: Int,
        val maxZ: Int
    )
    
    data class BlockPosition(val x: Int, val y: Int, val z: Int)
    
    data class BlockData(
        val material: Material,
        val data: Byte
    )
    
    /**
     * Capture arena snapshot asynchronously
     * Uses compression for memory efficiency
     */
    fun captureArena(arenaName: String, min: Location, max: Location): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val blocks = mutableMapOf<BlockPosition, BlockData>()
            
            val minX = Math.min(min.blockX, max.blockX)
            val minY = Math.min(min.blockY, max.blockY)
            val minZ = Math.min(min.blockZ, max.blockZ)
            val maxX = Math.max(min.blockX, max.blockX)
            val maxY = Math.max(min.blockY, max.blockY)
            val maxZ = Math.max(min.blockZ, max.blockZ)
            
            var blockCount = 0
            
            // Capture blocks
            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    for (z in minZ..maxZ) {
                        val block = min.world.getBlockAt(x, y, z)
                        
                        // Skip air for memory efficiency
                        if (block.type != Material.AIR) {
                            blocks[BlockPosition(x, y, z)] = BlockData(
                                block.type,
                                block.data
                            )
                            blockCount++
                        }
                    }
                }
            }
            
            val snapshot = ArenaSnapshot(blocks, minX, minY, minZ, maxX, maxY, maxZ)
            arenaSnapshots[arenaName] = snapshot
            
            plugin.logger.info("§a[ArenaRegen] Captured $arenaName: $blockCount blocks")
            
            // Save to disk (compressed)
            saveSnapshot(arenaName, snapshot)
        }
    }
    
    /**
     * Regenerate arena ULTRA FAST
     * Uses chunked async regeneration
     */
    fun regenerateArena(arenaName: String): CompletableFuture<Void> {
        val snapshot = arenaSnapshots[arenaName]
            ?: return CompletableFuture.completedFuture(null)
        
        return CompletableFuture.runAsync {
            val startTime = System.currentTimeMillis()
            var blocksRestored = 0
            
            // Phase 1: Clear area (set all to AIR) - SYNC
            plugin.server.scheduler.runTask(plugin, Runnable {
                for (x in snapshot.minX..snapshot.maxX) {
                    for (y in snapshot.minY..snapshot.maxY) {
                        for (z in snapshot.minZ..snapshot.maxZ) {
                            val block = plugin.server.worlds[0].getBlockAt(x, y, z)
                            block.type = Material.AIR
                        }
                    }
                }
            })
            
            // Phase 2: Restore blocks in chunks - ASYNC with batching
            val chunkSize = 1000
            val blockList = snapshot.blocks.entries.toList()
            
            for (i in blockList.indices step chunkSize) {
                val chunk = blockList.subList(i, Math.min(i + chunkSize, blockList.size))
                
                plugin.server.scheduler.runTask(plugin, Runnable {
                    chunk.forEach { (pos, data) ->
                        val block = plugin.server.worlds[0].getBlockAt(pos.x, pos.y, pos.z)
                        block.type = data.material
                        block.data = data.data
                        blocksRestored++
                    }
                })
                
                // Small delay between chunks to prevent lag
                Thread.sleep(10)
            }
            
            val duration = System.currentTimeMillis() - startTime
            plugin.logger.info("§a[ArenaRegen] Regenerated $arenaName in ${duration}ms ($blocksRestored blocks)")
        }
    }
    
    /**
     * Save snapshot to disk (compressed)
     */
    private fun saveSnapshot(arenaName: String, snapshot: ArenaSnapshot) {
        try {
            val file = File(plugin.dataFolder, "arenas/$arenaName.arena")
            file.parentFile.mkdirs()
            
            ObjectOutputStream(GZIPOutputStream(FileOutputStream(file))).use { oos ->
                oos.writeObject(snapshot)
            }
            
            plugin.logger.info("§a[ArenaRegen] Saved $arenaName to disk")
        } catch (e: Exception) {
            plugin.logger.severe("§c[ArenaRegen] Failed to save $arenaName: ${e.message}")
        }
    }
    
    /**
     * Load snapshot from disk
     */
    fun loadSnapshot(arenaName: String): Boolean {
        try {
            val file = File(plugin.dataFolder, "arenas/$arenaName.arena")
            if (!file.exists()) return false
            
            ObjectInputStream(GZIPInputStream(FileInputStream(file))).use { ois ->
                @Suppress("UNCHECKED_CAST")
                val snapshot = ois.readObject() as ArenaSnapshot
                arenaSnapshots[arenaName] = snapshot
            }
            
            plugin.logger.info("§a[ArenaRegen] Loaded $arenaName from disk")
            return true
        } catch (e: Exception) {
            plugin.logger.severe("§c[ArenaRegen] Failed to load $arenaName: ${e.message}")
            return false
        }
    }
    
    /**
     * Duplicate arena to new location
     */
    fun duplicateArena(sourceArena: String, targetLocation: Location, newName: String): CompletableFuture<Void> {
        val snapshot = arenaSnapshots[sourceArena]
            ?: return CompletableFuture.completedFuture(null)
        
        return CompletableFuture.runAsync {
            val offsetX = targetLocation.blockX - snapshot.minX
            val offsetY = targetLocation.blockY - snapshot.minY
            val offsetZ = targetLocation.blockZ - snapshot.minZ
            
            val newBlocks = mutableMapOf<BlockPosition, BlockData>()
            
            snapshot.blocks.forEach { (pos, data) ->
                val newPos = BlockPosition(
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ
                )
                newBlocks[newPos] = data
            }
            
            val newSnapshot = ArenaSnapshot(
                newBlocks,
                snapshot.minX + offsetX,
                snapshot.minY + offsetY,
                snapshot.minZ + offsetZ,
                snapshot.maxX + offsetX,
                snapshot.maxY + offsetY,
                snapshot.maxZ + offsetZ
            )
            
            arenaSnapshots[newName] = newSnapshot
            saveSnapshot(newName, newSnapshot)
            
            plugin.logger.info("§a[ArenaRegen] Duplicated $sourceArena to $newName")
        }
    }
}