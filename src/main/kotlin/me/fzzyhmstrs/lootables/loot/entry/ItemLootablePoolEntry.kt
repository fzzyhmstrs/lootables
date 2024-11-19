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
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.ItemLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.number.ConstantLootableNumber
import me.fzzyhmstrs.lootables.loot.number.LootableNumber
import net.minecraft.component.ComponentChanges
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.Vec3d
import kotlin.math.min

/**
 * Drops one pre-defined item with optional count and components.
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class ItemLootablePoolEntry private constructor(private val itemEntryStack: ItemEntryStack, private val dropItems: Boolean = false): LootablePoolEntry {

    /**
     * Drops one pre-defined itemstack.
     * @param stack [ItemStack] the item to drop; will drop a new item stack instance with the matching features (item, count, components) as this input stack
     * @param dropItems Boolean whether items are scattered on the ground or directly given to the player. Default false. When false, will give to inventory, when true will scatter.
     * @author fzzyhmstrs
     * @since 0.1.0
     */
    @JvmOverloads
    constructor(stack: ItemStack, dropItems: Boolean = false): this(ItemEntryStack(stack.registryEntry, ConstantLootableNumber(stack.count.toFloat()), stack.componentChanges), dropItems)

    /**
     * Drops an item with defined count and components. Each drop will be a new itemstack instance.
     * @param item [Item] the item to drop
     * @param count [LootableNumber] the potentially variable count of the item stack. Will be reevaluated on every drop
     * @param components [ComponentChanges] the components to apply to the stack
     * @param dropItems Boolean whether items are scattered on the ground or directly given to the player. Default false. When false, will give to inventory, when true will scatter.
     * @author fzzyhmstrs
     * @since 0.1.0
     */
    @JvmOverloads
    constructor(item: Item, count: LootableNumber, components: ComponentChanges, dropItems: Boolean = false): this(ItemEntryStack(item.registryEntry, count, components), dropItems)

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ITEM
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        if (dropItems) {
            ItemScatterer.spawn(player.world, origin.x, origin.y, origin.z, itemEntryStack.getStack())
        } else {
            player.inventory.offerOrDrop(itemEntryStack.getStack())
        }
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return ItemLootablePoolEntryDisplay(itemEntryStack.item, itemEntryStack.desc().string, itemEntryStack.avg().toByte(), itemEntryStack.hasGlint(), dropItems)
    }

    internal companion object {

        private val ITEM_CODEC: Codec<ItemEntryStack> = Codec.withAlternative(ItemEntryStack.CODEC, ItemEntryStack.INLINE_CODEC)

        val CODEC: MapCodec<ItemLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<ItemLootablePoolEntry> ->
            instance.group(
                ITEM_CODEC.fieldOf("item").forGetter(ItemLootablePoolEntry::itemEntryStack),
                Codec.BOOL.optionalFieldOf("drop_items", false).forGetter(ItemLootablePoolEntry::dropItems)
            ).apply(instance, ::ItemLootablePoolEntry)
        }

        fun createRandomInstance(playerEntity: ServerPlayerEntity): LootablePoolEntry {
            var item = Registries.ITEM.entrySet.random().value
            while (item == Items.AIR) {
                item = Registries.ITEM.entrySet.random().value
            }
            return ItemLootablePoolEntry(ItemStack(item, Lootables.random().nextInt(min(64, item.maxCount)) + 1), Lootables.random().nextBoolean())
        }
    }

    private class ItemEntryStack(val item: RegistryEntry<Item>, private val count: LootableNumber, private val components: ComponentChanges) {

        fun getStack(): ItemStack {
            return ItemStack(item, count.nextInt(), components)
        }

        fun displayStack(): ItemStack {
            return ItemStack(item, count.descInt(), components)
        }

        fun hasGlint(): Boolean {
            return getStack().hasGlint()
        }

        fun desc(): Text {
            return count.desc(true)
        }

        fun name(): Text {
            return item.value().name
        }

        fun avg(): Int {
            return count.descInt()
        }

        companion object {

            val INLINE_CODEC: Codec<ItemEntryStack> = ItemStack.ITEM_CODEC.xmap({ re -> ItemEntryStack(re, ConstantLootableNumber(1f), ComponentChanges.EMPTY) }, { i -> i.item})

            val CODEC: Codec<ItemEntryStack> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<ItemEntryStack> ->
                instance.group(
                    ItemStack.ITEM_CODEC.fieldOf("id").forGetter(ItemEntryStack::item),
                    LootableNumber.CODEC.fieldOf("count").forGetter(ItemEntryStack::count),
                    ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY).forGetter(ItemEntryStack::components)
                ).apply(instance, ::ItemEntryStack)
            }
        }

    }

}
