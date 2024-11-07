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

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.ItemLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.display.PoolLootablePoolEntryDisplay
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.LootTables
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.function.Consumer

class ExperienceLootablePoolEntry(private val xp: Int, private val levels: Boolean = true): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.XP
    }

    override fun apply(player: PlayerEntity, origin: Vec3d) {
        if (levels) {
            player.addExperienceLevels(xp)
        } else {
            player.addExperience(xp)
        }
    }

    override fun defaultDescription(): Text {
        return if(levels) "lootables.entry.xp.levels".translate(xp) else "lootables.entry.xp.points".translate(xp)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return ExperienceLootablePoolEntryDisplay(levels)
    }

    companion object {

        val CODEC: MapCodec<ExperienceLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<ExperienceLootablePoolEntry> ->
            instance.group(
                Codec.INT.fieldOf("xp").forGetter(ExperienceLootablePoolEntry::xp),
                Codec.BOOL.optionalFieldOf("levels", true).forGetter(ExperienceLootablePoolEntry::levels)
            ).apply(instance, ::ExperienceLootablePoolEntry)
        }
    }

}
