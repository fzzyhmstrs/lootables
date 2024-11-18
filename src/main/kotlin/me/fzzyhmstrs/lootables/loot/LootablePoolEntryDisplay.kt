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

package me.fzzyhmstrs.lootables.loot

import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import java.util.Optional

@JvmDefaultWithCompatibility
interface LootablePoolEntryDisplay {
    fun type(): LootablePoolEntryType
    fun provideIcons(): List<TileIcon>
    fun clientDescription(): Text? {
        return null
    }

    companion object {
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, LootablePoolEntryDisplay> = LootablePoolEntryType.PACKET_CODEC.dispatch({ display -> display.type() }, {type -> type.s2c()})
    }

    data class DisplayWithDesc(val display: LootablePoolEntryDisplay, val desc: Text?) {

        fun provideDescription(): Text {
            return desc ?: display.clientDescription() ?: FcText.translatable("lootables.entry.no.desc")
        }

        companion object {
            val PACKET_CODEC: PacketCodec<RegistryByteBuf, DisplayWithDesc> = PacketCodec.tuple(
                LootablePoolEntryDisplay.PACKET_CODEC,
                DisplayWithDesc::display,
                PacketCodecs.optional(TextCodecs.PACKET_CODEC),
                { dwd -> Optional.ofNullable(dwd.desc) },
                { d, o -> DisplayWithDesc(d, o.orElse(null)) }
            )
        }
    }
}