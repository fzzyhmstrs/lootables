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
import me.fzzyhmstrs.lootables.api.LootableItem
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import java.util.function.Supplier

object ComponentRegistry {

    fun init() {}

    private val lootableDataRegister = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Lootables.identity("data"),
        ComponentType.builder<LootableItem.LootableData>().codec(LootableItem.LootableData.CODEC).packetCodec(LootableItem.LootableData.PACKET_CODEC).cache().build()
    )
    val LOOTABLE_DATA: Supplier<ComponentType<LootableItem.LootableData>> = Supplier { lootableDataRegister }


}