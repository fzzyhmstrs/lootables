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
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.ExperienceLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.number.ConstantLootableNumber
import me.fzzyhmstrs.lootables.loot.number.LootableNumber
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

/**
 * Grants the player XP levels or points.
 * @param xp [LootableNumber] the amount of xp to give
 * @param levels Boolean; when true will grant the xp as levels, when false as points
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class ExperienceLootablePoolEntry @JvmOverloads constructor(private val xp: LootableNumber, private val levels: Boolean = true): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.XP
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        if (levels) {
            player.addExperienceLevels(xp.nextInt())
        } else {
            player.addExperience(xp.nextInt())
        }
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return ExperienceLootablePoolEntryDisplay(xp.desc(true).string, levels)
    }

    internal companion object {

        val CODEC: MapCodec<ExperienceLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<ExperienceLootablePoolEntry> ->
            instance.group(
                LootableNumber.CODEC.fieldOf("xp").forGetter(ExperienceLootablePoolEntry::xp),
                Codec.BOOL.optionalFieldOf("levels", true).forGetter(ExperienceLootablePoolEntry::levels)
            ).apply(instance, ::ExperienceLootablePoolEntry)
        }

        fun createRandomInstance(playerEntity: ServerPlayerEntity): LootablePoolEntry {
            return ExperienceLootablePoolEntry(ConstantLootableNumber(Lootables.random().nextFloat() * 100f), Lootables.random().nextBoolean())
        }
    }

}
