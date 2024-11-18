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

import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text

data class MultiLootablePoolEntryDisplay(private val children: List<LootablePoolEntryDisplay.DisplayWithDesc>): LootablePoolEntryDisplay {

    constructor(vararg child: LootablePoolEntryDisplay): this(child.toList().map { LootablePoolEntryDisplay.DisplayWithDesc(it, null) })

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.MULTI
    }

    override fun clientDescription(): Text {
        val text = FcText.translatable("lootables.entry.multi")
        text.append(FcText.literal("\n"))
        for ((i, child) in children.withIndex()) {
            text.append(child.provideDescription())
            if (i != children.lastIndex) {
                text.append(FcText.literal("\n"))
            }
        }
        return text
    }

    private val icons by lazy {
        children.map { it.display.provideIcons() }.stream().collect({ mutableListOf() }, { list, newList -> list.addAll(newList) }, MutableList<TileIcon>::addAll )
    }

    override fun provideIcons(): List<TileIcon> {
        return icons
    }

    companion object {
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, MultiLootablePoolEntryDisplay> = LootablePoolEntryDisplay.DisplayWithDesc.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(
            ::MultiLootablePoolEntryDisplay,
            MultiLootablePoolEntryDisplay::children
        )
    }
}