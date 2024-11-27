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
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.LootableItem.LootableData
import me.fzzyhmstrs.lootables.loot.LootableRarity
import me.fzzyhmstrs.lootables.registry.ComponentRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.world.World
import java.util.*

/**
 * Template item class with automatic component-based support for Lootables tables
 *
 * The drops from this item are controlled by the LOOTABLE_DATA component attached to the item. You can register one item, and provide dynamic components (and model predicates etc. as needed), rather than registering one item per specific. Or register a bunch, I'm not your dad.
 *
 * For items with PICKUP style loot, it is generally good practice to have max count be 1.
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
        val pickup = stack.getOrDefault(ComponentRegistry.PICKED_UP.get(), false)
        if (data.eventType != LootableData.EventType.USE && !pickup) return TypedActionResult.pass(stack)

        if (data.rollType == LootableData.RollType.RANDOM) {
            if(!LootablesApi.supplyLootRandomly(data.table, user, user.pos.add(0.0, 1.0, 0.0), data.key, data.rolls)) {
                return TypedActionResult.pass(stack)
            } else {
                stack.decrement(1)
            }
        } else {
            val originalStack = stack.copyWithCount(1)
            if(!LootablesApi.supplyLootWithChoices(data.table, user, user.pos.add(0.0, 1.0, 0.0), { _, _ -> stack.set(ComponentRegistry.PICKED_UP.get(), false) }, { p, _ -> p.inventory.offerOrDrop(originalStack) }, data.key, data.rolls)) {
                return TypedActionResult.pass(stack)
            } else {
                stack.decrement(1)
            }
        }
        return TypedActionResult.success(stack)
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        pickup(stack, world, entity)
    }

    fun pickup(stack: ItemStack, world: World, entity: Entity): Boolean {
        if (entity !is ServerPlayerEntity) return false
        val data = stack.get(ComponentRegistry.LOOTABLE_DATA.get()) ?: return false
        if (data == LootableData.DEFAULT) return false
        if (data.eventType != LootableData.EventType.PICKUP) return false
        val pickup = stack.getOrDefault(ComponentRegistry.PICKED_UP.get(), false)
        if (pickup) return false
        stack.set(ComponentRegistry.PICKED_UP.get(), true)

        if (data.rollType == LootableData.RollType.RANDOM) {
            if(!LootablesApi.supplyLootRandomly(data.table, entity, entity.pos.add(0.0, 1.0, 0.0), data.key, data.rolls)) {
                return false
            } else {
                stack.decrement(1)
            }
        } else {
            val originalStack = stack.copyWithCount(1)
            if(!LootablesApi.supplyLootWithChoices(data.table, entity, entity.pos.add(0.0, 1.0, 0.0), { _, _ -> stack.set(ComponentRegistry.PICKED_UP.get(), false) }, { p, pos -> ItemScatterer.spawn(world, pos.x, pos.y, pos.z, originalStack) }, data.key, data.rolls)) {
                return false
            } else {
                stack.decrement(1)
            }
        }
        return true
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        val data = stack.get(ComponentRegistry.LOOTABLE_DATA.get()) ?: return
        if (data == LootableData.DEFAULT) return
        val pickup = stack.getOrDefault(ComponentRegistry.PICKED_UP.get(), false)
        if (data.eventType == LootableData.EventType.USE || pickup) {
            val keybind: MutableText = Text.keybind("key.mouse.right")
            tooltip.add(FcText.translatable("lootables.item.use", keybind))
        }
    }

    /**
     * Component storing loot features of a [LootableItem]. This can be applied in the Item Settings, providing default lootables behavior for your item.
     * @see Builder - construct instances of this component with the builder.
     * @author fzzyhmstrs
     * @since 0.1.0
     */
    data class LootableData private constructor(val eventType: EventType, val rollType: RollType, val table: Identifier, val rolls: Int, val choices: Int, val rarity: LootableRarity? = null, val key: IdKey? = null) {

        private constructor(eventType: EventType, rollType: RollType, table: Identifier, rolls: Int, choices: Int, rarity: Optional<LootableRarity>, key: Optional<IdKey>):
                this(eventType, rollType, table, rolls, choices, rarity.orElse(null), key.orElse(null))

        /**
         * Defines when and how the [LootableItem] drops loot.
         * @author fzzyhmstrs
         * @since 0.1.0
         */
        enum class EventType(private val id: String): StringIdentifiable {
            /**
             * Loot is rolled when the item is used. The item is consumed on use.
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            USE("use"),
            /**
             * Loot is rolled when the item is picked up.
             * @author fzzyhmstrs
             * @since 0.1.0
             */
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

        /**
         * Defines how loot is rolled and given to the player
         * @author fzzyhmstrs
         * @since 0.1.0
         */
        enum class RollType(private val id: String): StringIdentifiable {
            /**
             * Loot is rolled randomly and directly inserted into the player inventory, like a vanilla loot table.
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            RANDOM("random"),
            /**
             * Loot is presented in the form of choice tiles like a loot box or incremental-progression style game.
             * @author fzzyhmstrs
             * @since 0.1.0
             */
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

        /**
         * Builds a [LootableData] for use as a component in a [LootableItem]
         * @param table [Identifier] resource location of the lootable table to roll
         * @author fzzyhmstrs
         * @since 0.1.0
         */
        class Builder internal constructor(private val table: Identifier) {
            private var eventType = EventType.USE
            private var rollType = RollType.RANDOM
            private var rolls = 1
            private var choices = 1
            private var rarity: LootableRarity? = null
            private var key: IdKey? = null

            /**
             * Sets this data to fire loot rolls when the item is picked up. The default behavior is to fire when the item is used (right click).
             *
             * If PICKUP is used, it's recommended not to use CHOICES loot mode. Having a screen pop up in the middle of a dungeon the instant you touch an item isn't great user experience.
             * @return this builder
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            fun onPickup(): Builder {
                this.eventType = EventType.PICKUP
                return this
            }

            /**
             * Sets this data to provide loot via choice tiles. Default behavior is to roll loot randomly (like a loot table).
             * @return this builder
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            fun choices(): Builder {
                this.rollType = RollType.CHOICES
                return this
            }

            /**
             * Defines the number of lootable pools rolled when the event is fired. Default is 1
             * - For RANDOM rolling, 1-3 rolls is recommended as a starting point for balancing.
             * - For CHOICES rolling 3-5 rolls is recommended, depending on the number of pools in the table and how many choices the player will be able to make.
             * @param rolls Int number of rolls the table will generate when the event fires.
             * @return this builder
             * @throws IllegalStateException if rolls are 0 or less
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            fun rolls(rolls: Int): Builder {
                if (rolls <= 0) throw IllegalStateException("Rolls must not be less than 1.")
                this.rolls = rolls
                return this
            }

            /**
             * Defines the number of lootable pools the player can pick when choices are presented to them. Default is 1. Only applicable to CHOICES mode.
             * - Number of choices should generally be a fraction of the number of rolls. Typical numbers used in games with this type of mechanic are 1:3 choices to rolls, or 2:5.
             * @param choices Int number of choices the player has from present choice tiles (rolls)
             * @return this builder
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            fun choices(choices: Int): Builder {
                this.choices = choices
                return this
            }

            fun rarity(rarity: LootableRarity): Builder {
                this.rarity = rarity
                return this
            }

            /**
             * Keys this loot to a specific IdKey. IdKeys restrict the number of times a particular player can access a particular loot roll.
             * @param key [IdKey] the key to restrict this loot with
             * @return this builder
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            fun keyed(key: IdKey): Builder {
                this.key = key
                return this
            }

            /**
             * Keys this loot to a specific IdKey. IdKeys restrict the number of times a particular player can access a particular loot roll.
             * @param keyId [Identifier] Unique identifier to key the loot event to
             * @param maxUses Int number of times the player may access the loot.
             * @return this builder
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            fun keyed(keyId: Identifier, maxUses: Int): Builder {
                this.key = IdKey(keyId, maxUses)
                return this
            }

            /**
             * Builds a [LootableData] based on inputs
             * @throws IllegalStateException if the loot mode is CHOICES and the choices count is <= 0
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            fun build(): LootableData {
                if (rollType == RollType.CHOICES && choices <= 0) throw IllegalStateException("Choices must be greater than 0 for CHOICES loot mode.")
                return LootableData(eventType, rollType, table, rolls, choices, rarity, key)
            }
        }

        companion object {

            /**
             * Builds a [LootableData] for use as a component in a [LootableItem]
             * @param table [Identifier] resource location of the lootable table to roll
             * @return [Builder]
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            @JvmStatic
            fun builder(table: Identifier): Builder {
                return Builder(table)
            }

            /**
             * Default LootableData Component. Not a valid set of data; the [LootableItem] will do nothing with this.
             * @author fzzyhmstrs
             * @since 0.1.0
             */
            @JvmStatic
            val DEFAULT = LootableData(EventType.USE, RollType.RANDOM, Lootables.identity("default"), 1, 1)

            @JvmStatic
            val CODEC = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<LootableData> ->
                instance.group(
                    EventType.CODEC.optionalFieldOf("event_type", EventType.USE).forGetter(LootableData::eventType),
                    RollType.CODEC.optionalFieldOf("roll_type", RollType.RANDOM).forGetter(LootableData::rollType),
                    Identifier.CODEC.fieldOf("table").forGetter(LootableData::table),
                    Codec.INT.optionalFieldOf("rolls", 1).forGetter(LootableData::rolls),
                    Codec.INT.optionalFieldOf("choices", 1).forGetter(LootableData::choices),
                    LootableRarity.CODEC.optionalFieldOf("rarity").forGetter{ ld -> Optional.ofNullable(ld.rarity) },
                    IdKey.CODEC.optionalFieldOf("key").forGetter{ ld -> Optional.ofNullable(ld.key) }
                ).apply(instance, ::LootableData)
            }

            @JvmStatic
            val PACKET_CODEC = object: PacketCodec<RegistryByteBuf, LootableData> {

                private val rarityCodec = PacketCodecs.optional(LootableRarity.PACKET_CODEC)
                private val keyCodec = PacketCodecs.optional(IdKey.PACKET_CODEC)

                override fun decode(buf: RegistryByteBuf): LootableData {
                    val a = EventType.PACKET_CODEC.decode(buf)
                    val b = RollType.PACKET_CODEC.decode(buf)
                    val c = Identifier.PACKET_CODEC.decode(buf)
                    val d = PacketCodecs.INTEGER.decode(buf)
                    val e = PacketCodecs.INTEGER.decode(buf)
                    val f = rarityCodec.decode(buf)
                    val g = keyCodec.decode(buf)
                    return LootableData(a, b, c, d, e, f, g)
                }

                override fun encode(buf: RegistryByteBuf, value: LootableData) {
                    EventType.PACKET_CODEC.encode(buf, value.eventType)
                    RollType.PACKET_CODEC.encode(buf, value.rollType)
                    Identifier.PACKET_CODEC.encode(buf, value.table)
                    PacketCodecs.INTEGER.encode(buf, value.rolls)
                    PacketCodecs.INTEGER.encode(buf, value.choices)
                    rarityCodec.encode(buf, Optional.ofNullable(value.rarity))
                    keyCodec.encode(buf, Optional.ofNullable(value.key))
                }

            }
        }
    }

}