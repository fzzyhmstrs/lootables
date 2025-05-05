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

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.cast
import me.fzzyhmstrs.fzzy_config.util.platform.RegistrySupplier
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.LootableItem
import net.minecraft.component.ComponentType
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries

@Suppress("UnstableApiUsage")
object ComponentRegistry {

    fun init() {
        REGISTRAR.init()
    }

    private val REGISTRAR = ConfigApi.platform().createRegistrar(Lootables.ID, Registries.DATA_COMPONENT_TYPE)

    val LOOTABLE_DATA: RegistrySupplier<ComponentType<LootableItem.LootableData>> = REGISTRAR.register("data") {  ComponentType.builder<LootableItem.LootableData>().codec(LootableItem.LootableData.CODEC).packetCodec(LootableItem.LootableData.PACKET_CODEC).cache().build() }.cast()
    val PICKED_UP: RegistrySupplier<ComponentType<Boolean>> = REGISTRAR.register("picked_up") { ComponentType.builder<Boolean>().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).cache().build() }.cast()
}