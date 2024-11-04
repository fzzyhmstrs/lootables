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

package me.fzzyhmstrs.lootables.loot

import me.fzzyhmstrs.lootables.Lootables
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.Util
import net.minecraft.util.function.ValueLists
import net.minecraft.util.math.ColorHelper
import java.awt.Color
import java.util.function.IntFunction
import java.util.function.Supplier

enum class LootableRarity(private val id: String, val weight: Int, val bgColor: Supplier<Int>, val startColor: Supplier<Int>, val endColor: Supplier<Int>, val bgHoveredColor: Supplier<Int>, val startHoveredColor: Supplier<Int>, val endHoveredColor: Supplier<Int>, val dividerId: Identifier): StringIdentifiable {
    COMMON("common",       12, { Colors.bgDark }, { 0xE0E0E0 },                 { 0xC4C4C4 },                { Colors.bgLight }, { 0xFFFFFF },              { 0xFFFFFF },              Lootables.identity("divider/common")),
    UNCOMMON("uncommon",   7,  { Colors.bgDark }, { 0x00A500 },                 { 0x007F00 },                { Colors.bgLight }, { 0x20D820 },              { 0x20D820 },              Lootables.identity("divider/uncommon")),
    RARE("rare",           4,  { Colors.bgDark }, { 0x4CFFFF },                 { 0x29A5A5 },                { Colors.bgLight }, { 0xB2FFFF },              { 0xB2FFFF },              Lootables.identity("divider/rare")),
    EPIC("epic",           2,  { Colors.bgDark }, { 0xFF55FF },                 { 0xBF39BF },                { Colors.bgLight }, { 0xFF99FF },              { 0xFF99FF },              Lootables.identity("divider/epic")),
    LEGENDARY("legendary", 1,  { Colors.bgDark }, { 0xFFAA00 },                 { 0xBF7900 },                { Colors.bgLight }, { 0xFFC34C },              { 0xFFC34C },              Lootables.identity("divider/legendary")),
    RAINBOW("rainbow",     1,  { Colors.bgDark }, { Colors.rainbow(0.85f) }, { Colors.rainbow(0.7f) }, { Colors.bgLight }, { Colors.rainbow(1f) }, { Colors.rainbow(1f) }, Lootables.identity("divider/rainbow"));


    override fun asString(): String {
        return id
    }

    object Colors {
        val bgDark = ColorHelper.Argb.getArgb(0xF0, 0x10, 0x00, 0x10)
        val bgLight = ColorHelper.Argb.getArgb(0xF0, 0x20, 0x00, 0x20)

        internal fun rainbow(b: Float): Int {
            val hue = (Util.getMeasuringTimeMs() % 2000L).toFloat() / 2000f
            return Color.HSBtoRGB(hue, 1f, b)
        }
    }

    internal companion object {
        internal val CODEC = StringIdentifiable.createCodec { entries.toTypedArray() }
        private val INDEX_TO_VALUE: IntFunction<LootableRarity> = ValueLists.createIdToValueFunction(
            LootableRarity::ordinal, entries.toTypedArray(), ValueLists.OutOfBoundsHandling.ZERO
        )
        internal val PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, LootableRarity::ordinal)
    }
}