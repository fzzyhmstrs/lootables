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

package me.fzzyhmstrs.lootables.loot.entry

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.PoolLootablePoolEntryDisplay
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootPool
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.function.Consumer

/**
 * Rolls a vanilla loot pool and provides the results to the player
 * @param pool [LootPool] the loot pool to roll
 * @param dropItems Boolean whether items are scattered on the ground or directly given to the player. Default false. When false, will give to inventory, when true will scatter.
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class PoolLootablePoolEntry @JvmOverloads constructor(private val pool: LootPool, private val dropItems: Boolean = false): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.POOL
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val consumer: Consumer<ItemStack> = if (dropItems) {
            Consumer { itemStack -> ItemScatterer.spawn(player.world, origin.x, origin.y, origin.z, itemStack.copy()) }
        } else {
            Consumer { itemStack -> player.inventory.offerOrDrop(itemStack.copy()) }
        }
        val params = LootContextParameterSet.Builder(player.serverWorld).add(LootContextParameters.THIS_ENTITY, player).add(LootContextParameters.ORIGIN, origin).luck(player.luck)
        val context = LootContext.Builder(params.build(LootContextTypes.CHEST)).build(Optional.empty())
        pool.addGeneratedLoot(consumer, context)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        val list: MutableList<ItemStack> = mutableListOf()
        val params = LootContextParameterSet.Builder(playerEntity.serverWorld).add(LootContextParameters.THIS_ENTITY, playerEntity).add(LootContextParameters.ORIGIN, playerEntity.pos).luck(playerEntity.luck)
        val context = LootContext.Builder(params.build(LootContextTypes.CHEST)).build(Optional.empty())
        pool.addGeneratedLoot({ itemStack -> list.add(itemStack) }, context)
        return PoolLootablePoolEntryDisplay(list, dropItems)
    }

    override fun needsInvalidation(type: LootablePoolEntry.InvalidationType): Boolean {
        return type == LootablePoolEntry.InvalidationType.INIT
    }

    internal companion object {

        val CODEC: MapCodec<PoolLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<PoolLootablePoolEntry> ->
            instance.group(
                LootPool.CODEC.fieldOf("pool").forGetter(PoolLootablePoolEntry::pool),
                Codec.BOOL.optionalFieldOf("drop_items", false).forGetter(PoolLootablePoolEntry::dropItems)
            ).apply(instance, ::PoolLootablePoolEntry)
        }
    }

}