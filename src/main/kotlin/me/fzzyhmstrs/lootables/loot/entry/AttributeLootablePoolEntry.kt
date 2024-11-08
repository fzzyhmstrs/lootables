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
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.AttributeLootablePoolEntryDisplay
import net.minecraft.component.type.AttributeModifiersComponent
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.Vec3d
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.function.Consumer

class AttributeLootablePoolEntry(private val attribute: RegistryEntry<EntityAttribute>, private val id: Identifier, private val value: Double, private val operation: Operation, private val persistent: Boolean = false): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ATTRIBUTE
    }

    override fun apply(player: PlayerEntity, origin: Vec3d) {
        val suffix = Lootables.random().nextLong().toString()
        if(persistent) {
            player.getAttributeInstance(attribute)?.addPersistentModifier(EntityAttributeModifier(id.withSuffixedPath(suffix), value, operation))
        } else {
            player.getAttributeInstance(attribute)?.addTemporaryModifier(EntityAttributeModifier(id.withSuffixedPath(suffix), value, operation))
        }
    }

    override fun defaultDescription(): Text {
        return if(persistent) "lootables.entry.attribute.persistent".translate(attributeDescription()) else "lootables.entry.attribute.temporary".translate(attributeDescription())
    }

    private fun attributeDescription(): Text {
        var d = value
        if(operation == Operation.ADD_MULTIPLIED_BASE || operation == Operation.ADD_MULTIPLIED_TOTAL) {
            d *= 100
        } else if (attribute.matches(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
            d *= 10
        }
        return if (d > 0.0) {
            FcText.translatable("attribute.modifier.plus." + operation.id,
                AttributeModifiersComponent.DECIMAL_FORMAT.format(d),
                Text.translatable(attribute.value().translationKey)
            ).formatted(attribute.value().getFormatting(true))
        } else if (d < 0.0) {
            FcText.translatable("attribute.modifier.take." + operation.id,
                AttributeModifiersComponent.DECIMAL_FORMAT.format(-d),
                Text.translatable(attribute.value().translationKey)
            ).formatted(attribute.value().getFormatting(false))
        } else {
            FcText.empty()
        }
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return AttributeLootablePoolEntryDisplay(attribute)
    }

    companion object {

        val DECIMAL_FORMAT = Util.make(DecimalFormat("#.##")) { format: DecimalFormat -> format.decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ROOT) }

        val CODEC: MapCodec<AttributeLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<AttributeLootablePoolEntry> ->
            instance.group(
                EntityAttribute.CODEC.fieldOf("attribute").forGetter(AttributeLootablePoolEntry::attribute),
                Identifier.CODEC.fieldOf("id").forGetter(AttributeLootablePoolEntry::id),
                Codec.DOUBLE.fieldOf("value").forGetter(AttributeLootablePoolEntry::value),
                Operation.CODEC.optionalFieldOf("operation", Operation.ADD_VALUE).forGetter(AttributeLootablePoolEntry::operation),
                Codec.BOOL.optionalFieldOf("persistent", false).forGetter(AttributeLootablePoolEntry::persistent)
            ).apply(instance, ::AttributeLootablePoolEntry)
        }
    }

}