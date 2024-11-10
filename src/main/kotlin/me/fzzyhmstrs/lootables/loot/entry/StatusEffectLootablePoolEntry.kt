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
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.HealLootablePoolEntryDisplay
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.screen.ScreenTexts
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class StatusEffectLootablePoolEntry(private val instance: StatusEffectInstance): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.STATUS
    }

    override fun apply(player: PlayerEntity, origin: Vec3d) {
        player.addStatusEffect(StatusEffectInstance(instance))
    }

    override fun defaultDescription(playerEntity: ServerPlayerEntity): Text {
        return "lootables.entry.status".translate(getStatusEffectDescription(instance), Lootables.DECIMAL_FORMAT.format(instance.duration / 20f))
    }

    private fun getStatusEffectDescription(statusEffect: StatusEffectInstance): Text {
        val mutableText = statusEffect.effectType.value().name.copy()
        if (statusEffect.amplifier in 1..9) {
            mutableText.append(ScreenTexts.SPACE).append(Text.translatable("enchantment.level." + (statusEffect.amplifier + 1)))
        }
        return mutableText
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return HealLootablePoolEntryDisplay
    }

    companion object {

        val CODEC: MapCodec<StatusEffectLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<StatusEffectLootablePoolEntry> ->
            instance.group(
                StatusEffectInstance.CODEC.fieldOf("status").forGetter(StatusEffectLootablePoolEntry::instance)
            ).apply(instance, ::StatusEffectLootablePoolEntry)
        }
    }

}