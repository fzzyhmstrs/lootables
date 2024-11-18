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

import com.mojang.serialization.MapCodec
import me.fzzyhmstrs.lootables.impl.LootablesApiImpl
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.CustomLootablePoolEntryDisplay
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class CustomLootablePoolEntry(private val id: Identifier): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.CUSTOM
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        LootablesApiImpl.getCustomEntry(id)?.apply(player, origin)
    }

    override fun serverDescription(playerEntity: ServerPlayerEntity): Text? {
        return LootablesApiImpl.getCustomEntry(id)?.serverDescription(playerEntity)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return CustomLootablePoolEntryDisplay(id)
    }

    companion object {
        val CODEC: MapCodec<CustomLootablePoolEntry> = Identifier.CODEC.fieldOf("id").xmap(
            ::CustomLootablePoolEntry,
            CustomLootablePoolEntry::id
        )
    }

}