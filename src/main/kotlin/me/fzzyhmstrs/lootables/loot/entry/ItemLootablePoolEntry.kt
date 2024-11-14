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
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.ItemLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.number.ConstantLootableNumber
import me.fzzyhmstrs.lootables.loot.number.LootableNumber
import net.minecraft.component.ComponentChanges
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.Vec3d

class ItemLootablePoolEntry private constructor(private val itemEntryStack: ItemEntryStack, private val dropItems: Boolean = false): LootablePoolEntry {

    constructor(stack: ItemStack, dropItems: Boolean = false): this(ItemEntryStack(stack.registryEntry, ConstantLootableNumber(stack.count.toFloat()), stack.componentChanges), dropItems)

    constructor(item: Item, count: LootableNumber, components: ComponentChanges, dropItems: Boolean = false): this(ItemEntryStack(item.registryEntry, count, components), dropItems)

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ITEM
    }

    override fun apply(player: PlayerEntity, origin: Vec3d) {
        if (dropItems) {
            ItemScatterer.spawn(player.world, origin.x, origin.y, origin.z, itemEntryStack.getStack())
        } else {
            player.inventory.offerOrDrop(itemEntryStack.getStack())
        }
    }

    override fun defaultDescription(playerEntity: ServerPlayerEntity): Text {
        return if(dropItems) "lootables.entry.item.drop".translate(itemEntryStack.desc(), itemEntryStack.name()) else "lootables.entry.item.give".translate(itemEntryStack.desc(), itemEntryStack.name())
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return ItemLootablePoolEntryDisplay(itemEntryStack.displayStack())
    }

    companion object {



        private val ITEM_CODEC: Codec<ItemEntryStack> = Codec.withAlternative(ItemEntryStack.CODEC, ItemEntryStack.INLINE_CODEC)

        val CODEC: MapCodec<ItemLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<ItemLootablePoolEntry> ->
            instance.group(
                ITEM_CODEC.fieldOf("item").forGetter(ItemLootablePoolEntry::itemEntryStack),
                Codec.BOOL.optionalFieldOf("drop_items", false).forGetter(ItemLootablePoolEntry::dropItems)
            ).apply(instance, ::ItemLootablePoolEntry)
        }
    }

    private class ItemEntryStack(private val item: RegistryEntry<Item>, private val count: LootableNumber, private val components: ComponentChanges) {

        fun getStack(): ItemStack {
            return ItemStack(item, count.nextInt(), components)
        }

        fun displayStack(): ItemStack {
            return ItemStack(item, count.descInt(), components)
        }

        fun desc(): Text {
            return count.desc(true)
        }

        fun name(): Text {
            return item.value().name
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