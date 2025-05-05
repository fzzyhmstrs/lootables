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
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.Lootables
import net.minecraft.text.Text

class ConfigLootableNumber(private val scope: String): LootableNumber {

    override fun nextFloat(): Float {
        return RESULT_SUPPLIER.getResult(scope)
    }

    override fun desc(asInt: Boolean): Text {
        val value = nextFloat()
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

        other as ConfigLootableNumber

        return scope == other.scope
    }

    override fun hashCode(): Int {
        return scope.hashCode()
    }

    companion object {

        private val RESULT_SUPPLIER = ConfigApi.result().createSimpleResultProvider(0f, Float::class)

        private val INLINE_CODEC: Codec<ConfigLootableNumber> = Codec.STRING.xmap(
            ::ConfigLootableNumber,
            ConfigLootableNumber::scope
        )

        private val FULL_CODEC: Codec<ConfigLootableNumber> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ConfigLootableNumber> ->
            instance.group(
                Codec.STRING.fieldOf("scope").forGetter(ConfigLootableNumber::scope)
            ).apply(instance, ::ConfigLootableNumber)
        }

        val CODEC: Codec<ConfigLootableNumber> = Codec.withAlternative(INLINE_CODEC, FULL_CODEC)
    }
}