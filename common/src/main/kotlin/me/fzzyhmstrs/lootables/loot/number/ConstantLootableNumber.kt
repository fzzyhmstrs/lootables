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

class ConstantLootableNumber(private val value: Float): LootableNumber {

    override fun nextFloat(): Float {
        return value
    }

    override fun desc(asInt: Boolean): Text {
        return if (asInt || value.compareTo(value.toInt()) == 0)
            FcText.literal(value.toInt().toString())
        else
            FcText.literal(Lootables.DECIMAL_FORMAT.format(value))
    }

    override fun descInt(): Int {
        return nextInt()
    }

    override fun descFloat(): Float {
        return nextFloat()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConstantLootableNumber

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        private val INLINE_CODEC: Codec<ConstantLootableNumber> = Codec.FLOAT.xmap(
            ::ConstantLootableNumber,
            ConstantLootableNumber::value
        )

        private val FULL_CODEC: Codec<ConstantLootableNumber> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ConstantLootableNumber> ->
            instance.group(
                Codec.FLOAT.fieldOf("value").forGetter(ConstantLootableNumber::value)
            ).apply(instance, ::ConstantLootableNumber)
        }

        val CODEC: Codec<ConstantLootableNumber> = Codec.withAlternative(INLINE_CODEC, FULL_CODEC)
    }
}