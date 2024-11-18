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

import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text

data class ItemLootablePoolEntryDisplay(private val item: RegistryEntry<Item>, private val count: String, private val avg: Byte, private val glint: Boolean, private val dropItems: Boolean): LootablePoolEntryDisplay {

    constructor(stack: ItemStack, dropItems: Boolean) : this(stack.registryEntry, "1", 1, stack.hasGlint(), dropItems)

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ITEM
    }

    override fun clientDescription(): Text {
        return if(dropItems) "lootables.entry.item.drop".translate(count, item.value().name) else "lootables.entry.item.give".translate(count, item.value().name)
    }

    override fun provideIcons(): List<TileIcon> {
        return listOf(TileIcon { context, x, y ->
            val itemStack = item.value().defaultStack.copyWithCount(avg.toInt())
            if (glint) {
                itemStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            }
            context.drawItem(itemStack, x + 1, y + 1)
            context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, itemStack, x + 1, y + 1)
        })
    }

    companion object {
        val PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryEntry(RegistryKeys.ITEM),
            ItemLootablePoolEntryDisplay::item,
            PacketCodecs.STRING,
            ItemLootablePoolEntryDisplay::count,
            PacketCodecs.BYTE,
            ItemLootablePoolEntryDisplay::avg,
            PacketCodecs.BOOL,
            ItemLootablePoolEntryDisplay::glint,
            PacketCodecs.BOOL,
            ItemLootablePoolEntryDisplay::dropItems,
            ::ItemLootablePoolEntryDisplay
        )
    }
}