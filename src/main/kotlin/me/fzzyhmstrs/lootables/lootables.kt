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

import me.fzzyhmstrs.lootables.config.LootablesConfig
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.LootablesData
import me.fzzyhmstrs.lootables.network.LootablesNetworking
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.random.Random
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


object Lootables: ModInitializer {
    const val MOD_ID = "lootables"
    val LOGGER: Logger = LoggerFactory.getLogger("lootables")
    private val random = Random.createThreadSafe()
    val DECIMAL_FORMAT by lazy {
        Util.make(DecimalFormat("#.##")) { format: DecimalFormat ->
            format.decimalFormatSymbols = DecimalFormatSymbols.getInstance(
                Locale.ROOT
            )
        }
    }

    override fun onInitialize() {
        LootablesConfig.init()
        LootablePoolEntryTypes.init()
        LootablesData.init()
        LootablesNetworking.init()
    }

    fun random(): Random {
        return random
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
        return Random.createThreadSafe()
    }
}