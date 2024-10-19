/*
 *
 *  Copyright (c) 2024 Fzzyhmstrs
 *
 *  This file is part of Lootables API , a mod made for minecraft; as such it falls under the license of Lootables API.
 *
 *  Lootables API is free software provided under the terms of the Timefall Development License - Modified (TDL-M).
 *  You should have received a copy of the TDL-M with this software.
 *  If you did not, see <https://github.com/fzzyhmstrs/Timefall-Development-Licence-Modified>.
 *
 */

package me.fzzyhmstrs.lootables.loot.entry

import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolData
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import java.util.*

class ItemLootablePoolEntry(private val itemStack: ItemStack, private val dropItems: Boolean): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        TODO("Not yet implemented")
    }

    override fun apply(player: PlayerEntity, origin: BlockPos) {
        if (dropItems) {
            val pos = origin.toCenterPos()
            ItemScatterer.spawn(player.world, pos.x, pos.y, pos.z, itemStack.copy())
        } else {
            player.inventory.offerOrDrop(itemStack.copy())
        }
    }

    override fun createData(descriptionOverride: Optional<Text>): LootablePoolData {
        TODO("Not yet implemented")
    }
}