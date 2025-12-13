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

package me.fzzyhmstrs.lootables.command

import com.mojang.brigadier.CommandDispatcher
import me.fzzyhmstrs.lootables.Lootables
import net.minecraft.command.argument.ArgumentTypes
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.command.ServerCommandSource
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object LootablesCommands {

    fun init(bus: IEventBus) {

        val registrar = DeferredRegister.create(RegistryKeys.COMMAND_ARGUMENT_TYPE, Lootables.ID)

        registrar.register(bus)

        registrar.register("lootable_table", Supplier {
            ArgumentTypes.registerByClass(
                LootableTableArgumentType::class.java,
                ConstantArgumentSerializer.of { _ -> LootableTableArgumentType() }
            )
        })

        NeoForge.EVENT_BUS.addListener(::registerCommands)
    }

    private fun registerCommands(event: RegisterCommandsEvent) {
        register(event.dispatcher)
    }

    private fun register(commandDispatcher: CommandDispatcher<ServerCommandSource>) {
        LootableCommandsCommon.register(commandDispatcher)
    }
}