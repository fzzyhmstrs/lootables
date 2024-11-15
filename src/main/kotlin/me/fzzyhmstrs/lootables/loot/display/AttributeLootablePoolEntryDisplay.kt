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

import me.fzzyhmstrs.fzzy_config.util.RenderUtil.drawTex
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.MissingSprite
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry

data class AttributeLootablePoolEntryDisplay(private val effect: RegistryEntry<EntityAttribute>): LootablePoolEntryDisplay {

    private var errorThrown = false

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ATTRIBUTE
    }

    override fun provideIcons(): List<TileIcon> {
        return listOf(TileIcon { context, x, y ->
            var id = Registries.ATTRIBUTE.getId(effect.value())?.withPath{ path -> "attribute/$path" } ?: Lootables.identity("attribute/unknown")
            val sprite = MinecraftClient.getInstance().guiAtlasManager.getSprite(id)
            if (sprite.contents.id == MissingSprite.getMissingSpriteId()) {
                if (!errorThrown) {
                    Lootables.LOGGER.error("Sprite for attribute ${Registries.ATTRIBUTE.getId(effect.value())} couldn't be found; using fallback")
                    errorThrown = true
                }
                id = Lootables.identity("attribute/unknown")
            }
            context.drawTex(id, x, y, 18, 18)
        })
    }

    companion object {
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, AttributeLootablePoolEntryDisplay> = EntityAttribute.PACKET_CODEC.xmap(
            ::AttributeLootablePoolEntryDisplay,
            AttributeLootablePoolEntryDisplay::effect
        )
    }
}