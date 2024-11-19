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

class BinomialLootableNumber(private val n: Int, private val p: Float): LootableNumber {

    override fun nextInt(): Int {
        var j = 0
        for (i in 0 until n) {
            if (Lootables.random().nextFloat() < p) {
                j++
            }
        }
        return j
    }

    override fun nextFloat(): Float {
        return nextInt().toFloat()
    }

    override fun desc(asInt: Boolean): Text {
        val mean = n * p
        return if (asInt)
            FcText.literal("~${mean.toInt()}")
        else
            FcText.literal("~${Lootables.DECIMAL_FORMAT.format(mean)}")
    }

    override fun descInt(): Int {
        return (n * p).toInt()
    }

    override fun descFloat(): Float {
        return n * p
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BinomialLootableNumber

        if (n != other.n) return false
        if (p != other.p) return false

        return true
    }

    override fun hashCode(): Int {
        var result = n
        result = 31 * result + p.hashCode()
        return result
    }

    companion object {
        val CODEC: Codec<BinomialLootableNumber> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<BinomialLootableNumber> ->
            instance.group(
                Codec.intRange(0, Int.MAX_VALUE).fieldOf("n").forGetter(BinomialLootableNumber::n),
                Codec.floatRange(0f, 1f).fieldOf("p").forGetter(BinomialLootableNumber::p)
            ).apply(instance, ::BinomialLootableNumber)
        }
    }
}