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
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.StatusEffectLootablePoolEntryDisplay
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

/**
 * Applies a status effect to the player.
 * @param instance [StatusEffectInstance] effect to apply to the player.
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class StatusEffectLootablePoolEntry(private val instance: StatusEffectInstance): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.STATUS
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        player.addStatusEffect(StatusEffectInstance(instance))
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return StatusEffectLootablePoolEntryDisplay(instance.effectType, instance.amplifier.toByte(), instance.duration)
    }

    internal companion object {

        val CODEC: MapCodec<StatusEffectLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<StatusEffectLootablePoolEntry> ->
            instance.group(
                StatusEffectInstance.CODEC.fieldOf("status").forGetter(StatusEffectLootablePoolEntry::instance)
            ).apply(instance, ::StatusEffectLootablePoolEntry)
        }
    }

}
