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
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.text.Text

data object CommandLootablePoolEntryDisplay: SimpleLootablePoolEntryDisplay(Lootables.identity("display/command")) {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.COMMAND
    }

    override fun clientDescription(): Text {
        return "lootables.entry.command".translate()
    }

    val PACKET_CODEC: PacketCodec<RegistryByteBuf, CommandLootablePoolEntryDisplay> = PacketCodec.unit(this)
}