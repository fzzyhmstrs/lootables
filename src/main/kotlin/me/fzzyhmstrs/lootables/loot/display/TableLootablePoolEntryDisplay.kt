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
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs

data class TableLootablePoolEntryDisplay(private val itemStacks: List<ItemStack>): LootablePoolEntryDisplay {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.TABLE
    }

    private val icons: List<TileIcon> by lazy {
        val stackListList: MutableList<MutableList<ItemStack>> = mutableListOf(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())
        for ((i, stack) in itemStacks.withIndex()) {
            stackListList[i % 4].add(stack)
        }
        stackListList.mapNotNull {
            if(it.isEmpty()) {
                null
            } else if (it.size == 1) {
                TileIcon { context, x, y ->
                    context.drawItem(it[0], x, y)
                    context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, it[0], x, y)
                }
            } else {
                TileIcon { context, x, y ->
                    val time = System.currentTimeMillis() / 1000L
                    val index = time % it.size
                    val itemStack = it[index.toInt()]
                    context.drawItem(itemStack, x, y)
                    context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, itemStack, x, y)
                }
            }
        }
    }

    override fun provideIcons(): List<TileIcon> {
        return icons
    }

    companion object {
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, TableLootablePoolEntryDisplay> = ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(
            ::TableLootablePoolEntryDisplay,
            TableLootablePoolEntryDisplay::itemStacks
        )
    }
}