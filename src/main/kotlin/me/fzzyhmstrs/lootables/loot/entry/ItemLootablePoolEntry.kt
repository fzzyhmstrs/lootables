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
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.Vec3d

class ItemLootablePoolEntry(private val itemStack: ItemStack, private val dropItems: Boolean = true): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ITEM
    }

    override fun apply(player: PlayerEntity, origin: Vec3d) {
        if (dropItems) {
            ItemScatterer.spawn(player.world, origin.x, origin.y, origin.z, itemStack.copy())
        } else {
            player.inventory.offerOrDrop(itemStack.copy())
        }
    }

    override fun defaultDescription(): Text {
        return if(dropItems) "lootables.entry.item.drop".translate(itemStack.count, itemStack.item.name) else "lootables.entry.item.give".translate(itemStack.count, itemStack.item.name)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return ItemLootablePoolEntryDisplay(itemStack)
    }

    companion object {

        private val ITEM_CODEC: Codec<ItemStack> = Codec.withAlternative(ItemStack.CODEC, ItemStack.ITEM_CODEC.xmap({ re -> ItemStack(re) }, { i -> i.item.registryEntry }))

        val CODEC: MapCodec<ItemLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<ItemLootablePoolEntry> ->
            instance.group(
                ITEM_CODEC.fieldOf("item").forGetter(ItemLootablePoolEntry::itemStack),
                Codec.BOOL.optionalFieldOf("drop_items", true).forGetter(ItemLootablePoolEntry::dropItems)
            ).apply(instance, ::ItemLootablePoolEntry)
        }
    }

}