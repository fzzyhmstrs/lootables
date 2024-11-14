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
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.Lootables
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

class UniformLootableNumber(private val min: Float, private val max: Float): LootableNumber {

    override fun nextFloat(): Float {
        return MathHelper.nextFloat(Lootables.random(), min, max)
    }

    override fun desc(asInt: Boolean): Text {
        return if (asInt)
            FcText.literal("${min.toInt()}-${max.toInt()}")
        else
            FcText.literal("${Lootables.DECIMAL_FORMAT.format(min)}-${Lootables.DECIMAL_FORMAT.format(max)}")
    }

    override fun descInt(): Int {
        return ((min + max) / 2f).toInt()
    }

    override fun descFloat(): Float {
        return ((min + max) / 2f)
    }

    companion object {
        val CODEC: Codec<UniformLootableNumber> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<UniformLootableNumber> ->
            instance.group(
                Codec.FLOAT.fieldOf("min").forGetter(UniformLootableNumber::min),
                Codec.FLOAT.fieldOf("max").forGetter(UniformLootableNumber::max)
            ).apply(instance, ::UniformLootableNumber)
        }
    }
}