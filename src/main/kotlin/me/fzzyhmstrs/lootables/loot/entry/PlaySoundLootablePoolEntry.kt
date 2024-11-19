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

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.PlaySoundLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.number.ConstantLootableNumber
import me.fzzyhmstrs.lootables.loot.number.LootableNumber
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class PlaySoundLootablePoolEntry(private val soundEvent: RegistryEntry<SoundEvent>, private val volume: LootableNumber, private val pitch: LootableNumber, private val child: LootablePoolEntry): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.PLAY_SOUND
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        player.world.playSound(player, origin.x, origin.y, origin.z, soundEvent, SoundCategory.PLAYERS, volume.nextFloat(), pitch.nextFloat())
        child.apply(player, origin)
    }

    override fun serverDescription(playerEntity: ServerPlayerEntity): Text? {
        return child.serverDescription(playerEntity)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return PlaySoundLootablePoolEntryDisplay(child.createDisplay(playerEntity))
    }

    companion object {

        val CODEC: MapCodec<PlaySoundLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<PlaySoundLootablePoolEntry> ->
            instance.group(
                SoundEvent.ENTRY_CODEC.fieldOf("sound").forGetter(PlaySoundLootablePoolEntry::soundEvent),
                LootableNumber.CODEC.optionalFieldOf("volume", ConstantLootableNumber(1f)).forGetter(PlaySoundLootablePoolEntry::volume),
                LootableNumber.CODEC.optionalFieldOf("pitch", ConstantLootableNumber(1f)).forGetter(PlaySoundLootablePoolEntry::pitch),
                LootablePoolEntry.CODEC.fieldOf("child").forGetter(PlaySoundLootablePoolEntry::child)
            ).apply(instance, ::PlaySoundLootablePoolEntry)
        }
    }

}