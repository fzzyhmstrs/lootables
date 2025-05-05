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

import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.fzzy_config.util.RenderUtil.drawTex
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.MissingSprite
import net.minecraft.component.type.AttributeModifiersComponent
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text

data class AttributeLootablePoolEntryDisplay(private val effect: RegistryEntry<EntityAttribute>, private val value: Float, private val operation: Operation, private val persistent: Boolean = false): LootablePoolEntryDisplay {

    private var errorThrown = false

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ATTRIBUTE
    }

    override fun clientDescription(): Text {
        return if(persistent) "lootables.entry.attribute.persistent".translate(attributeDescription()) else "lootables.entry.attribute.temporary".translate(attributeDescription())
    }

    private fun attributeDescription(): Text {
        var d = value
        @Suppress("DEPRECATION")
        if(operation == Operation.ADD_MULTIPLIED_BASE || operation == Operation.ADD_MULTIPLIED_TOTAL) {
            d *= 100
        } else if (effect.matches(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
            d *= 10
        }
        return if (d > 0.0) {
            FcText.translatable("attribute.modifier.plus." + operation.id,
                AttributeModifiersComponent.DECIMAL_FORMAT.format(d),
                Text.translatable(effect.value().translationKey)
            ).formatted(effect.value().getFormatting(true))
        } else if (d < 0.0) {
            FcText.translatable("attribute.modifier.take." + operation.id,
                AttributeModifiersComponent.DECIMAL_FORMAT.format(-d),
                Text.translatable(effect.value().translationKey)
            ).formatted(effect.value().getFormatting(false))
        } else {
            FcText.empty()
        }
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
        val PACKET_CODEC: PacketCodec<RegistryByteBuf, AttributeLootablePoolEntryDisplay> = PacketCodec.tuple(
            EntityAttribute.PACKET_CODEC,
            AttributeLootablePoolEntryDisplay::effect,
            PacketCodecs.FLOAT,
            AttributeLootablePoolEntryDisplay::value,
            Operation.PACKET_CODEC,
            AttributeLootablePoolEntryDisplay::operation,
            PacketCodecs.BOOL,
            AttributeLootablePoolEntryDisplay::persistent,
            ::AttributeLootablePoolEntryDisplay
        )
    }
}