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
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.text.Text

data class LootFunctionLootablePoolEntryDisplay(private val slots: AttributeModifierSlot): SimpleLootablePoolEntryDisplay(Lootables.identity("display/function")) {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.FUNCTION
    }

    override fun clientDescription(): Text? {
        return "lootables.entry.function".translate(slots.asString())
    }

    companion object {
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, LootFunctionLootablePoolEntryDisplay> = AttributeModifierSlot.PACKET_CODEC.xmap(
            ::LootFunctionLootablePoolEntryDisplay,
            LootFunctionLootablePoolEntryDisplay::slots
        ).cast()
    }

}