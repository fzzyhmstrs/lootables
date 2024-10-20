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

package me.fzzyhmstrs.lootables.loot

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.EitherMapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.math.BlockPos
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.*

class LootablePool private constructor(
    internal val id: Identifier,
    internal val rarity: LootableRarity,
    internal val entry: LootablePoolEntry,
    private val description: Optional<Text>,
    private val weight: Optional<Int>,
    private val maxUses: Int = -1,
    private val conditions: List<LootCondition> = listOf())
{

    fun canApply(context: LootContext): Boolean {
        val entity = context.get(LootContextParameters.THIS_ENTITY) ?: return false
        val uses = LootablesData.getUses(id, entity.uuid)
        if (conditions.isEmpty()) return true
        if (maxUses in 1..uses) return false
        for (condition in conditions) {
            if (!condition.test(context)) return false
        }
        return true
    }

    fun apply(player: PlayerEntity, origin: Vec3d) {
        LootablesData.use(id, player.uuid)
        this.entry.apply(player, origin)
    }

    fun getWeight(): Int {
        return weight.orElse(rarity.weight)
    }

    fun createData(): LootablePoolData {
        return LootablePoolData(id, description.orElse(entry.defaultDescription()), entry.createDisplay())
    }

    companion object {
        private val CONDITION_CODEC: MapCodec<List<LootCondition>> = EitherMapCodec(LootCondition.CODEC.listOf().fieldOf("if"), LootCondition.CODEC.fieldOf("if")).xmap(
            {e -> e.map({l -> l}, {c -> listOf(c)})},
            {l -> if(l.size != 1) Either.left(l) else Either.right(l[0]) }
        )

        val CODEC: Codec<LootablePool> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<LootablePool> ->
            instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(LootablePool::id),
                LootableRarity.CODEC.optionalFieldOf("rarity", LootableRarity.COMMON).forGetter(LootablePool::rarity),
                LootablePoolEntry.MAP_CODEC.codec().fieldOf("entry").forGetter(LootablePool::entry),
                TextCodecs.CODEC.optionalFieldOf("desc").forGetter(LootablePool::description),
                Codec.intRange(1, Int.MAX_VALUE).optionalFieldOf("weight").orElse(Optional.empty()).forGetter(LootablePool::weight),
                Codec.INT.optionalFieldOf("max_uses", -1).forGetter(LootablePool::maxUses),
                CONDITION_CODEC.forGetter(LootablePool::conditions)
            ).apply(instance, ::LootablePool)
        }
    }
}