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

package me.fzzyhmstrs.lootables.loot.function

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.registry.LootFunctionRegistry
import net.minecraft.item.ItemStack
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.function.ConditionalLootFunction
import net.minecraft.loot.function.LootFunctionType
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProviderTypes
import net.minecraft.util.math.MathHelper
import java.util.*
import kotlin.math.max

class RepairEquipmentLootFunction(conditions: MutableList<LootCondition>, private val repairPercent: Optional<LootNumberProvider>, private val repairAmount: Optional<LootNumberProvider>) : ConditionalLootFunction(conditions) {

    override fun getType(): LootFunctionType<RepairEquipmentLootFunction> {
        return LootFunctionRegistry.REPAIR_EQUIPMENT.get()
    }

    override fun process(stack: ItemStack, context: LootContext): ItemStack {
        if (!stack.isDamageable || !stack.isDamaged) return stack
        if (repairPercent.isPresent) {
            val percent = MathHelper.clamp(repairPercent.get().nextFloat(context), 0f, 1f)
            val repair = (stack.maxDamage * percent).toInt()
            stack.damage -= repair
        }
        if (repairAmount.isPresent) {
            val repair = max(0, repairAmount.get().nextInt(context))
            stack.damage -= repair
        }
        return stack
    }

    companion object {
        val CODEC: MapCodec<RepairEquipmentLootFunction> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<RepairEquipmentLootFunction> ->
            addConditionsField(instance).and(
                instance.group(
                    LootNumberProviderTypes.CODEC.optionalFieldOf("percent").forGetter(RepairEquipmentLootFunction::repairPercent),
                    LootNumberProviderTypes.CODEC.optionalFieldOf("amount").forGetter(RepairEquipmentLootFunction::repairAmount)
                )
            ).apply(instance, ::RepairEquipmentLootFunction)
        }
    }
}