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

import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.network.codec.PacketCodecs
import java.util.*

data class AdvancementLootablePoolEntryDisplay(private val display: Optional<ItemStack>): SimpleLootablePoolEntryDisplay(Lootables.identity("display/advancement")) {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ADVANCEMENT
    }

    override fun provideIcons(): List<TileIcon> {
        return if (display.isPresent)
                listOf(TileIcon { context, x, y ->
                    context.drawItem(display.get(), x + 1, y + 1)
                    context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, display.get(), x + 1, y + 1)
                })
            else
                super.provideIcons()
    }

    companion object {
        val PACKET_CODEC = PacketCodecs.optional(ItemStack.PACKET_CODEC).xmap(
            ::AdvancementLootablePoolEntryDisplay,
            AdvancementLootablePoolEntryDisplay::display
        )
    }
}