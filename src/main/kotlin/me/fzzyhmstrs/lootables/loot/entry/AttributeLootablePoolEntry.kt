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
import me.fzzyhmstrs.lootables.loot.display.AttributeLootablePoolEntryDisplay
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*

/**
 * Grants a temporary (until death, dimension change, or relog) or permanent attribute buff.
 * @param attribute [RegistryEntry]&lt;[EntityAttribute]&gt; attribute to buff
 * @param value Double value to buff the attribute by
 * @param operation [Operation] how to apply the buff. See Minecraft wiki for details on what the different operations do internally.
 * @param persistent Boolean; when true will be a permanent buff, when false will be temporary.
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class AttributeLootablePoolEntry @JvmOverloads constructor(private val attribute: RegistryEntry<EntityAttribute>, private val value: Double, private val operation: Operation, private val persistent: Boolean = false): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.ATTRIBUTE
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val suffix = UUID.randomUUID().toString().lowercase()
        if(persistent) {
            player.getAttributeInstance(attribute)?.addPersistentModifier(EntityAttributeModifier(Lootables.identity(suffix), value, operation))
        } else {
            player.getAttributeInstance(attribute)?.addTemporaryModifier(EntityAttributeModifier(Lootables.identity(suffix), value, operation))
        }
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return AttributeLootablePoolEntryDisplay(attribute, value.toFloat(), operation, persistent)
    }

    internal companion object {

        val CODEC: MapCodec<AttributeLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<AttributeLootablePoolEntry> ->
            instance.group(
                EntityAttribute.CODEC.fieldOf("attribute").forGetter(AttributeLootablePoolEntry::attribute),
                Codec.DOUBLE.fieldOf("value").forGetter(AttributeLootablePoolEntry::value),
                Operation.CODEC.optionalFieldOf("operation", Operation.ADD_VALUE).forGetter(AttributeLootablePoolEntry::operation),
                Codec.BOOL.optionalFieldOf("persistent", false).forGetter(AttributeLootablePoolEntry::persistent)
            ).apply(instance, ::AttributeLootablePoolEntry)
        }

        fun createRandomInstance(playerEntity: ServerPlayerEntity): LootablePoolEntry {
            return AttributeLootablePoolEntry(Registries.ATTRIBUTE.getEntry(Lootables.random().nextInt(Registries.ATTRIBUTE.size())).orElseThrow(), Lootables.random().nextDouble() * 10.0, Operation.entries[Lootables.random().nextInt(3)], Lootables.random().nextBoolean())
        }
    }

}
