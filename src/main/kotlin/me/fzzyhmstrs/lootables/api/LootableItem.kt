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

package me.fzzyhmstrs.lootables.api

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.LootableItem.LootableData
import me.fzzyhmstrs.lootables.registry.ComponentRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.*
import net.minecraft.world.World
import java.util.*

/**
 * Template item class with automatic component-based support for Lootables tables
 *
 * The drops from this item are controlled by the LOOTABLE_DATA component attached to the item. You can register one item, and provide dynamic components (and model predicates etc. as needed), rather than registering one item per specific. Or register a bunch, I'm not your dad.
 * @param settings [Item.Settings] standard settings for an item. The component for your lootables drops should be passed in here.
 * @see LootableData
 * @see [ComponentRegistry.LOOTABLE_DATA]
 * @author fzzyhmstrs
 * @since 0.1.0
 */
open class LootableItem(settings: Settings) : Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (user !is ServerPlayerEntity) return TypedActionResult.pass(stack)
        val data = stack.get(ComponentRegistry.LOOTABLE_DATA.get()) ?: return TypedActionResult.pass(stack)
        if (data == LootableData.DEFAULT) return TypedActionResult.pass(stack)
        if (data.eventType != LootableData.EventType.USE) return TypedActionResult.pass(stack)

        if (data.rollType == LootableData.RollType.RANDOM) {
            if(!LootablesApi.supplyLootRandomly(data.table, user, user.pos.add(0.0, 1.0, 0.0), data.key, data.rolls)) {
                return TypedActionResult.pass(stack)
            } else {
                stack.decrement(1)
            }
        } else {
            val originalStack = stack.copy()
            if(!LootablesApi.supplyLootWithChoices(data.table, user, user.pos.add(0.0, 1.0, 0.0), { _, _ -> }, { p, _ -> p.inventory.offerOrDrop(originalStack) }, data.key, data.rolls)) {
                return TypedActionResult.pass(stack)
            } else {
                stack.decrement(1)
            }
        }
        return TypedActionResult.success(stack)
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (entity !is ServerPlayerEntity) return
        val data = stack.get(ComponentRegistry.LOOTABLE_DATA.get()) ?: return
        if (data == LootableData.DEFAULT) return
        if (data.eventType != LootableData.EventType.PICKUP) return
        if (data.rollType == LootableData.RollType.RANDOM) {
            if(!LootablesApi.supplyLootRandomly(data.table, entity, entity.pos.add(0.0, 1.0, 0.0), data.key, data.rolls)) {
                return
            } else {
                stack.decrement(1)
            }
        } else {
            val originalStack = stack.copy()
            if(!LootablesApi.supplyLootWithChoices(data.table, entity, entity.pos.add(0.0, 1.0, 0.0), { _, _ -> }, { p, pos -> ItemScatterer.spawn(world, pos.x, pos.y, pos.z, originalStack) }, data.key, data.rolls)) {
                return
            } else {
                stack.decrement(1)
            }
        }
    }


    data class LootableData(val eventType: EventType, val rollType: RollType, val table: Identifier, val rolls: Int, val choices: Int, val key: IdKey? = null) {

        constructor(eventType: EventType, rollType: RollType, table: Identifier, rolls: Int, choices: Int, key: Optional<IdKey>):
                this(eventType, rollType, table, rolls, choices, key.orElse(null))

        enum class EventType(private val id: String): StringIdentifiable {
            USE("use"),
            PICKUP("pickup");

            override fun asString(): String {
                return id
            }

            companion object {
                val CODEC: Codec<EventType> = StringIdentifiable.createCodec { EventType.entries.toTypedArray() }

                val PACKET_CODEC: PacketCodec<RegistryByteBuf, EventType> = PacketCodecs.BOOL.xmap(
                    { bool -> if(bool) PICKUP else USE },
                    { et -> et != USE }
                ).cast()
            }
        }

        enum class RollType(private val id: String): StringIdentifiable {
            RANDOM("random"),
            CHOICES("choices");

            override fun asString(): String {
                return id
            }

            companion object {
                val CODEC: Codec<RollType> = StringIdentifiable.createCodec { RollType.entries.toTypedArray() }

                val PACKET_CODEC: PacketCodec<RegistryByteBuf, RollType> = PacketCodecs.BOOL.xmap(
                    { bool -> if(bool) CHOICES else RANDOM },
                    { et -> et != RANDOM }
                ).cast()
            }
        }

        companion object {

            @JvmStatic
            val DEFAULT = LootableData(EventType.USE, RollType.RANDOM, Lootables.identity("default"), 1, 1)

            internal val CODEC = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<LootableData> ->
                instance.group(
                    EventType.CODEC.fieldOf("event_type").forGetter(LootableData::eventType),
                    RollType.CODEC.fieldOf("roll_type").forGetter(LootableData::rollType),
                    Identifier.CODEC.fieldOf("table").forGetter(LootableData::table),
                    Codec.INT.optionalFieldOf("rolls", 3).forGetter(LootableData::rolls),
                    Codec.INT.optionalFieldOf("choices", 1).forGetter(LootableData::choices),
                    IdKey.CODEC.optionalFieldOf("key").forGetter{ ld -> Optional.ofNullable(ld.key) }
                ).apply(instance, ::LootableData)
            }

            internal val PACKET_CODEC = PacketCodec.tuple(
                EventType.PACKET_CODEC,
                LootableData::eventType,
                RollType.PACKET_CODEC,
                LootableData::rollType,
                Identifier.PACKET_CODEC,
                LootableData::table,
                PacketCodecs.INTEGER,
                LootableData::rolls,
                PacketCodecs.INTEGER,
                LootableData::choices,
                PacketCodecs.optional(IdKey.PACKET_CODEC),
                { ld -> Optional.ofNullable(ld.key) },
                ::LootableData
            )
        }
    }

}