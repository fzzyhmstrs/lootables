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

package me.fzzyhmstrs.lootables.registry

import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.function.RepairEquipmentLootFunction
import net.minecraft.loot.function.LootFunctionType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import java.util.function.Supplier

object LootFunctionRegistry {

    fun init() {}

    private val repairEquipmentRegister = Registry.register(
        Registries.LOOT_FUNCTION_TYPE,
        Lootables.identity("repair_equipment"),
        LootFunctionType(RepairEquipmentLootFunction.CODEC)
    )

    val REPAIR_EQUIPMENT: Supplier<LootFunctionType<RepairEquipmentLootFunction>> = Supplier { repairEquipmentRegister }

}