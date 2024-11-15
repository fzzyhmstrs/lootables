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
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.HealLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.number.ConstantLootableNumber
import me.fzzyhmstrs.lootables.loot.number.LootableNumber
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class HealLootablePoolEntry(private val amount: LootableNumber): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.HEAL
    }

    override fun apply(player: PlayerEntity, origin: Vec3d) {
        player.heal(amount.nextFloat())
    }

    override fun defaultDescription(playerEntity: ServerPlayerEntity): Text {
        return "lootables.entry.heal".translate(Lootables.DECIMAL_FORMAT.format(amount.descFloat() / 2f))
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return HealLootablePoolEntryDisplay
    }

    companion object {

        val CODEC: MapCodec<HealLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<HealLootablePoolEntry> ->
            instance.group(
                LootableNumber.CODEC.fieldOf("amount").forGetter(HealLootablePoolEntry::amount)
            ).apply(instance, ::HealLootablePoolEntry)
        }

        fun createRandomInstance(playerEntity: ServerPlayerEntity): LootablePoolEntry {
            return ExperienceLootablePoolEntry(ConstantLootableNumber(Lootables.random().nextFloat() * 100f), Lootables.random().nextBoolean())
        }
    }

}