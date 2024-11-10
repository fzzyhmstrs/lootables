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

import io.netty.buffer.ByteBuf
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.impl.LootablesApiImpl
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.Identifier

class CustomLootablePoolEntryDisplay(private val id: Identifier): LootablePoolEntryDisplay {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.CUSTOM
    }

    override fun provideIcons(): List<TileIcon> {
        return LootablesApiImpl.getCustomEntry(id)?.createDisplay()?.provideIcons() ?: listOf()
    }

    companion object {
        val PACKET_CODEC: PacketCodec<ByteBuf, CustomLootablePoolEntryDisplay> = Identifier.PACKET_CODEC.xmap(
            ::CustomLootablePoolEntryDisplay,
            CustomLootablePoolEntryDisplay::id
        )
    }
}