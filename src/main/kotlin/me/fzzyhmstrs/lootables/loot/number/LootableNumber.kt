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

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
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

        val CODEC: Codec<LootableNumber> = NumberCodec()

        private class NumberCodec: Codec<LootableNumber> {

            private val constant = ConstantLootableNumber.CODEC.cast<Codec<LootableNumber>>()
            private val uniform = UniformLootableNumber.CODEC.cast<Codec<LootableNumber>>()
            private val binomial = BinomialLootableNumber.CODEC.cast<Codec<LootableNumber>>()
            private val either = EitherLootableNumber.CODEC.cast<Codec<LootableNumber>>()
            private val triangular = TriangularLootableNumber.CODEC.cast<Codec<LootableNumber>>()

            override fun <T : Any?> encode(input: LootableNumber, ops: DynamicOps<T>, prefix: T): DataResult<T> {
                return when (input) {
                    is ConstantLootableNumber -> constant.encode(input, ops, prefix)
                    is UniformLootableNumber -> uniform.encode(input, ops, prefix)
                    is BinomialLootableNumber -> binomial.encode(input, ops, prefix)
                    is EitherLootableNumber -> either.encode(input, ops, prefix)
                    is TriangularLootableNumber -> triangular.encode(input, ops, prefix)
                    else -> DataResult.error { "Failed to encode Lootable Number. ${input::class.java.simpleName} is not a known number type." }
                }
            }

            override fun <T : Any?> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<LootableNumber, T>> {
                val constantResult = constant.decode(ops, input)
                if (constantResult.isSuccess) {
                    return constantResult
                }
                val uniformResult = uniform.decode(ops, input)
                if (uniformResult.isSuccess) {
                    return uniformResult
                }
                val binomialResult = binomial.decode(ops, input)
                if (uniformResult.isSuccess) {
                    return binomialResult
                }
                val eitherResult = either.decode(ops, input)
                if (eitherResult.isSuccess) {
                    return eitherResult
                }
                val triangularResult = triangular.decode(ops, input)
                if (triangularResult.isSuccess) {
                    return triangularResult
                }
                return DataResult.error { "Failed to parse Lootable Number. Const: ${constantResult.error().map { it.messageSupplier.get() }.orElse("none")}, Uniform: ${uniformResult.error().map { it.messageSupplier.get() }.orElse("none")}, Binomial: ${binomialResult.error().map { it.messageSupplier.get() }.orElse("none")}, Either: ${eitherResult.error().map { it.messageSupplier.get() }.orElse("none")}, Triangular: ${triangularResult.error().map { it.messageSupplier.get() }.orElse("none")}" }
            }
        }
    }

}