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

class EitherLootableNumber(private val either: Float, private val or: Float, private val weight: Float): LootableNumber {

    override fun nextFloat(): Float {
        return if (Lootables.random().nextFloat() < weight) either else or
    }

    override fun desc(asInt: Boolean): Text {
        return if (asInt)
            FcText.translatable("lootables.number.either", either.toInt(), or.toInt())
        else
            FcText.translatable("lootables.number.either", Lootables.DECIMAL_FORMAT.format(either), Lootables.DECIMAL_FORMAT.format(or))
    }

    override fun descInt(): Int {
        return MathHelper.lerp(weight, or, either).toInt()
    }

    override fun descFloat(): Float {
        return MathHelper.lerp(weight, or, either)
    }

    companion object {
        val CODEC: Codec<EitherLootableNumber> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<EitherLootableNumber> ->
            instance.group(
                Codec.FLOAT.fieldOf("either").forGetter(EitherLootableNumber::either),
                Codec.FLOAT.fieldOf("or").forGetter(EitherLootableNumber::or),
                Codec.floatRange(0f, 1f).optionalFieldOf("weight", 0.5f).forGetter(EitherLootableNumber::weight)
            ).apply(instance, ::EitherLootableNumber)
        }
    }
}