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

package me.fzzyhmstrs.lootables.config

import me.fzzyhmstrs.fzzy_config.annotations.NonSync
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import me.fzzyhmstrs.lootables.Lootables

class LootablesConfig: Config(Lootables.identity("lootables")) {

    @NonSync
    var easeInTiles = true

    @NonSync
    @ValidatedInt.Restrict(0, 20)
    var easeInDuration = 4

    @NonSync
    @ValidatedInt.Restrict(0, 50)
    var easeInAmount = 10

    @NonSync
    var animateTileHover = true

    @NonSync
    @ValidatedInt.Restrict(0, 10)
    var hoverDelay = 2

    companion object {
        val INSTANCE = ConfigApi.registerAndLoadConfig({ LootablesConfig() })
    }

}