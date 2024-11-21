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
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.HealLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.display.SpawnParticlesLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.number.ConstantLootableNumber
import me.fzzyhmstrs.lootables.loot.number.LootableNumber
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleType
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*

/**
 * Cosmetic Entry. Spawns a "burst" of particles before applying the "real" child entry.
 * @param particle [ParticleEffect] the particle to spawn
 * @param count [LootableNumber] how many particles to spawn. You'll probably need more than you think. The defaul codec amount is 100
 * @param radius [Radius] the size of burst the particles spawn in
 * @param speed [Optional]&lt;[LootableNumber]&gt; how fast particles move away from the burst. Empty optional = 0.
 * @param child [LootablePoolEntry] the "real" entry to apply after particles are spawned.
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class SpawnParticlesLootablePoolEntry(private val particle: ParticleEffect, private val count: LootableNumber, private val radius: Radius, private val speed: Optional<LootableNumber>, private val child: LootablePoolEntry): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.PARTICLE
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        player.serverWorld.spawnParticles(
            particle,
            origin.x,
            origin.y,
            origin.z,
            count.nextInt(),
            radius.x.nextFloat().toDouble(),
            radius.y.nextFloat().toDouble(),
            radius.z.nextFloat().toDouble(),
            speed.map { it.nextFloat() }.orElse(0f).toDouble())
        child.apply(player, origin)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return SpawnParticlesLootablePoolEntryDisplay(child.createDisplay(playerEntity))
    }

    override fun needsInvalidation(type: LootablePoolEntry.InvalidationType): Boolean {
        return child.needsInvalidation(type)
    }

    internal companion object {

        val CODEC: MapCodec<SpawnParticlesLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<SpawnParticlesLootablePoolEntry> ->
            instance.group(
                ParticleTypes.TYPE_CODEC.fieldOf("particle").forGetter(SpawnParticlesLootablePoolEntry::particle),
                LootableNumber.CODEC.optionalFieldOf("count", ConstantLootableNumber(100f)).forGetter(SpawnParticlesLootablePoolEntry::count),
                Radius.CODEC.optionalFieldOf("radius", Radius()).forGetter(SpawnParticlesLootablePoolEntry::radius),
                LootableNumber.CODEC.optionalFieldOf("speed").forGetter(SpawnParticlesLootablePoolEntry::speed),
                LootablePoolEntry.CODEC.fieldOf("child").forGetter(SpawnParticlesLootablePoolEntry::child)
            ).apply(instance, ::SpawnParticlesLootablePoolEntry)
        }
    }

    class Radius(val x: LootableNumber, val y: LootableNumber, val z: LootableNumber) {

        constructor(r: LootableNumber): this(r, r, r)

        constructor(): this(ConstantLootableNumber(1f))

        companion object {

            private val SIMPLE_CODEC = LootableNumber.CODEC.flatComapMap(
                { ln -> Radius(ln, ln, ln) },
                { r -> if (r.x == r.y && r.y == r.z) DataResult.success(r.x) else DataResult.error { "Not a simple Radius" } }
            )

            private val COMPLEX_CODEC = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<Radius> ->
                instance.group(
                    LootableNumber.CODEC.fieldOf("x").forGetter(Radius::x),
                    LootableNumber.CODEC.fieldOf("y").forGetter(Radius::y),
                    LootableNumber.CODEC.fieldOf("z").forGetter(Radius::z)
                ).apply(instance, ::Radius)
            }

            val CODEC = Codec.withAlternative(SIMPLE_CODEC, COMPLEX_CODEC)
        }
    }

}