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

package me.fzzyhmstrs.lootables.loot.display

import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack

class ItemLootablePoolEntryDisplay(private val itemStack: ItemStack): LootablePoolEntryDisplay {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ITEM
    }

    override fun provideIcons(): List<TileIcon> {
        return listOf(TileIcon { context, x, y ->
            context.drawItem(itemStack, x, y)
            context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, itemStack, x, y)
        })
    }

    companion object {
        val PACKET_CODEC = ItemStack.PACKET_CODEC.xmap(
            ::ItemLootablePoolEntryDisplay,
            ItemLootablePoolEntryDisplay::itemStack
        )
    }
}