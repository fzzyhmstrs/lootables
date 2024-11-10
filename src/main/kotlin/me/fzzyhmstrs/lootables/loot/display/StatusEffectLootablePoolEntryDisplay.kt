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

package me.fzzyhmstrs.lootables.loot.display

import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.registry.entry.RegistryEntry

class StatusEffectLootablePoolEntryDisplay(private val effect: RegistryEntry<StatusEffect>): LootablePoolEntryDisplay {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.STATUS
    }

    private val icons by lazy {
        listOf(TileIcon { context, x, y ->
            val sprite = MinecraftClient.getInstance().statusEffectSpriteManager.getSprite(effect)
            context.drawSprite(x, y, 0, 18, 18, sprite)
        })
    }

    override fun provideIcons(): List<TileIcon> {
        return icons
    }

    companion object {
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, StatusEffectLootablePoolEntryDisplay> = StatusEffect.ENTRY_PACKET_CODEC.xmap(
            ::StatusEffectLootablePoolEntryDisplay,
            StatusEffectLootablePoolEntryDisplay::effect
        )
    }
}