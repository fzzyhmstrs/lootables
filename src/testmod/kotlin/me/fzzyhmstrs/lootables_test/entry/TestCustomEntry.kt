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

package me.fzzyhmstrs.lootables_test.entry

import me.fzzyhmstrs.lootables.loot.custom.CustomLootableEntry
import net.minecraft.entity.AreaEffectCloudEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

object TestCustomEntry: CustomLootableEntry {

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val entity = AreaEffectCloudEntity(player.world, origin.x, origin.y, origin.z)
        entity.owner = player
        entity.particleType = ParticleTypes.CAMPFIRE_COSY_SMOKE
        entity.radius = 16.0f
        entity.duration = 100
        entity.radiusGrowth = 0f
        entity.addEffect(StatusEffectInstance(StatusEffects.SATURATION, 100))
        player.world.spawnEntity(entity)
    }

    override fun serverDescription(playerEntity: ServerPlayerEntity): Text? {
        return null
    }
}