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

class TriangularLootableNumber(private val a: Float, private val b: Float): LootableNumber {

    override fun nextFloat(): Float {
        val roll = MathHelper.sqrt(Lootables.random().nextFloat())
        return MathHelper.lerp(roll, a, b)
    }

    override fun desc(asInt: Boolean): Text {
        return if (asInt)
            FcText.literal("${a.toInt()}-${b.toInt()}")
        else
            FcText.literal("${Lootables.DECIMAL_FORMAT.format(a)}-${Lootables.DECIMAL_FORMAT.format(b)}")
    }

    override fun descInt(): Int {
        return ((a + b + b) / 3f).toInt()
    }

    override fun descFloat(): Float {
        return ((a + b + b) / 3f)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TriangularLootableNumber

        if (a != other.a) return false
        if (b != other.b) return false

        return true
    }

    override fun hashCode(): Int {
        var result = a.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }

    companion object {
        val CODEC: Codec<TriangularLootableNumber> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<TriangularLootableNumber> ->
            instance.group(
                Codec.FLOAT.fieldOf("a").forGetter(TriangularLootableNumber::a),
                Codec.FLOAT.fieldOf("b").forGetter(TriangularLootableNumber::b)
            ).apply(instance, ::TriangularLootableNumber)
        }
    }
}