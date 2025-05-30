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

import com.mojang.datafixers.util.Either
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.EitherMapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.LootFunctionLootablePoolEntryDisplay
import net.minecraft.component.type.AttributeModifierSlot
import net.minecraft.entity.EquipmentSlot
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.LootFunctionTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.function.Function

/**
 * Applies loot functions (item modifiers) to one or more equipped items on the player
 * @param functions List&lt;[LootFunction]&gt; functions to be applied to the players gear
 * @param relevantSlots [AttributeModifierSlot] defines which equipment slot or slots is relevant to the functions. Default MAINHAND
 * @see [me.fzzyhmstrs.lootables.loot.function.RepairEquipmentLootFunction]
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class LootFunctionLootablePoolEntry(private val functions: List<LootFunction>, private val relevantSlots: AttributeModifierSlot = AttributeModifierSlot.MAINHAND): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.FUNCTION
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val params = LootContextParameterSet.Builder(player.serverWorld).add(LootContextParameters.THIS_ENTITY, player).add(LootContextParameters.ORIGIN, origin).luck(player.luck)
        val context = LootContext.Builder(params.build(LootContextTypes.CHEST)).build(Optional.empty())
        for (slot in EquipmentSlot.entries) {
            if (!relevantSlots.matches(slot)) continue
            for (function in functions) {
                val stack = player.getEquippedStack(slot)
                if (stack.isEmpty) continue
                function.apply(stack, context)
            }
        }
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return LootFunctionLootablePoolEntryDisplay(relevantSlots)
    }

    internal companion object {

        private val SINGLE_FUNCTION_CODEC: MapCodec<List<LootFunction>> = LootFunctionTypes.CODEC.fieldOf("function").flatXmap(
            { lf -> DataResult.success(mutableListOf(lf)) },
            { li -> if (li.size == 1) DataResult.success(li[0]) else DataResult.error { "List size must be 1" } }
        )
        private val MULTIPLE_FUNCTION_CODEC: MapCodec<List<LootFunction>> = LootFunctionTypes.CODEC.listOf().fieldOf("functions")

        private val FUNCTION_CODEC = EitherMapCodec(SINGLE_FUNCTION_CODEC, MULTIPLE_FUNCTION_CODEC).xmap(
            { e -> e.map(Function.identity(), Function.identity()) },
            { li -> if (li.size == 1) Either.left(li) else Either.right(li) }
        )

        val CODEC: MapCodec<LootFunctionLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<LootFunctionLootablePoolEntry> ->
            instance.group(
                FUNCTION_CODEC.forGetter(LootFunctionLootablePoolEntry::functions),
                AttributeModifierSlot.CODEC.optionalFieldOf("slots", AttributeModifierSlot.MAINHAND).forGetter(LootFunctionLootablePoolEntry::relevantSlots)
            ).apply(instance, ::LootFunctionLootablePoolEntry)
        }
    }

}