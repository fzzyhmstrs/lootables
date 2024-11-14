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

package me.fzzyhmstrs.lootables.loot.number

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_config.cast
import net.minecraft.text.Text

interface LootableNumber {
    fun nextInt(): Int {
        return nextFloat().toInt()
    }
    fun nextFloat(): Float
    fun desc(asInt: Boolean): Text
    fun descInt(): Int {
        return descFloat().toInt()
    }
    fun descFloat(): Float

    companion object {
        private val CODEC_1: Codec<LootableNumber> = Codec.withAlternative(ConstantLootableNumber.CODEC.cast(), UniformLootableNumber.CODEC)
        private val CODEC_2: Codec<LootableNumber> = Codec.withAlternative(EitherLootableNumber.CODEC.cast(), BinomialLootableNumber.CODEC)
        private val CODEC_3: Codec<LootableNumber> = Codec.withAlternative(CODEC_2, TriangularLootableNumber.CODEC)
        val CODEC: Codec<LootableNumber> = Codec.withAlternative(CODEC_1, CODEC_3)
    }

}