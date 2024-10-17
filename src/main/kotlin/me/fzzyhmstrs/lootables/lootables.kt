@file:Suppress("PropertyName")

/*
* Copyright (c) 2024 Fzzyhmstrs
*
* This file is part of Lootables API , a mod made for minecraft; as such it falls under the license of Lootables API.
*
* Lootables API is free software provided under the terms of the Timefall Development License - Modified (TDL-M).
* You should have received a copy of the TDL-M with this software.
* If you did not, see <https://github.com/fzzyhmstrs/Timefall-Development-Licence-Modified>.
* */

package me.fzzyhmstrs.lootables

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random


object Lootables: ModInitializer {
    const val MOD_ID = "lootables"
    val LOGGER: Logger = LoggerFactory.getLogger("lootables")
    override fun onInitialize() {
    }

    fun random(): Random {
        return Random(System.currentTimeMillis())
    }

    fun identity(path: String): Identifier {
        return Identifier.of(MOD_ID, path)
    }
}

@Environment(value = EnvType.CLIENT)
object LootablesClient: ClientModInitializer {

    override fun onInitializeClient() {
    }

    fun random(): Random {
        return Random(System.currentTimeMillis())
    }
}