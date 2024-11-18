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
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.AdvancementLootablePoolEntryDisplay
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.*

class AdvancementLootablePoolEntry(private val advancement: Identifier): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ADVANCEMENT
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val adv = player.serverWorld.server.advancementLoader.get(advancement)
        if (adv == null) {
            Lootables.LOGGER.error("Advancement Lootable Pool Entry couldn't find advancement $advancement")
            return
        }
        for (criterion in player.advancementTracker.getProgress(adv).unobtainedCriteria) {
            player.advancementTracker.grantCriterion(adv, criterion)
        }
    }

    override fun serverDescription(playerEntity: ServerPlayerEntity): Text {
        val adv = playerEntity.serverWorld.server.advancementLoader.get(advancement)?.value?.display?.map { it.title }?.orElse(FcText.literal(advancement.toString())) ?: FcText.literal(advancement.toString())
        return "lootables.entry.advancement".translate(adv)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        val adv = playerEntity.serverWorld.server.advancementLoader.get(advancement)
        if (adv == null) {
            Lootables.LOGGER.error("Advancement Lootable Display couldn't find advancement $advancement")
            return AdvancementLootablePoolEntryDisplay(Optional.empty())
        }
        return AdvancementLootablePoolEntryDisplay(adv.value.display.map { it.icon })
    }

    /*override */

    companion object {

        val CODEC: MapCodec<AdvancementLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<AdvancementLootablePoolEntry> ->
            instance.group(
                Identifier.CODEC.fieldOf("advancement").forGetter(AdvancementLootablePoolEntry::advancement)
            ).apply(instance, ::AdvancementLootablePoolEntry)
        }

        fun createRandomInstance(playerEntity: ServerPlayerEntity): LootablePoolEntry {
            return AdvancementLootablePoolEntry(playerEntity.serverWorld.server.advancementLoader.advancements.random().id)
        }
    }

}