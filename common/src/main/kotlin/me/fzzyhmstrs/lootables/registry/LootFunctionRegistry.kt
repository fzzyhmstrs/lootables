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

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.cast
import me.fzzyhmstrs.fzzy_config.util.platform.RegistrySupplier
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.function.RepairEquipmentLootFunction
import net.minecraft.loot.function.LootFunctionType
import net.minecraft.registry.Registries

@Suppress("UnstableApiUsage")
object LootFunctionRegistry {

    fun init() {
        REGISTRAR.init()
    }

    private val REGISTRAR = ConfigApi.platform().createRegistrar(Lootables.ID, Registries.LOOT_FUNCTION_TYPE)

    val REPAIR_EQUIPMENT: RegistrySupplier<LootFunctionType<RepairEquipmentLootFunction>> = REGISTRAR.register("repair_equipment") { LootFunctionType(RepairEquipmentLootFunction.CODEC) }.cast()

}