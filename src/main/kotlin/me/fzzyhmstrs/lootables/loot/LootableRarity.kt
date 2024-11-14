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

import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.Lootables
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.Util
import net.minecraft.util.function.ValueLists
import net.minecraft.util.math.ColorHelper
import java.awt.Color
import java.util.function.IntFunction
import java.util.function.Supplier

enum class LootableRarity(private val id: String, val weight: Int, val bgColor: Supplier<Int>, val startColor: Supplier<Int>, val endColor: Supplier<Int>, val bgHoveredColor: Supplier<Int>, val startHoveredColor: Supplier<Int>, val endHoveredColor: Supplier<Int>, val dividerId: Identifier, val gradientOpacity: Float, val decoration: DecorationType): StringIdentifiable {
    COMMON("common",       12, { Colors.bgDark }, { Colors.color(0xA0A0A0) },   { Colors.color(0x646464) },  { Colors.bgLight }, { Colors.color(0xFFFFFF) }, { Colors.color(0xFFFFFF) }, Lootables.identity("divider/common"),    0f,     DecorationType.NONE),
    UNCOMMON("uncommon",   6,  { Colors.bgDark }, { Colors.color(0x00A500) },   { Colors.color(0x004F00) },  { Colors.bgLight }, { Colors.color(0x20D820) }, { Colors.color(0x20D820) }, Lootables.identity("divider/uncommon"),  0f,     DecorationType.NONE),
    RARE("rare",           3,  { Colors.bgDark }, { Colors.color(0x4CFFFF) },   { Colors.color(0x097575) },  { Colors.bgLight }, { Colors.color(0xA2EFEF) }, { Colors.color(0xA2EFEF) }, Lootables.identity("divider/rare"),      0.075f, DecorationType.NONE),
    EPIC("epic",           2,  { Colors.bgDark }, { Colors.color(0xFF55FF) },   { Colors.color(0x7F197F) },  { Colors.bgLight }, { Colors.color(0xFF99FF) }, { Colors.color(0xFF99FF) }, Lootables.identity("divider/epic"),      0.15f,  DecorationType.SMALL),
    LEGENDARY("legendary", 1,  { Colors.bgDark }, { Colors.color(0xFFAA00) },   { Colors.color(0x9F5900) },  { Colors.bgLight }, { Colors.color(0xFFC34C) }, { Colors.color(0xFFC34C) }, Lootables.identity("divider/legendary"), 0.25f,  DecorationType.BIG),
    RAINBOW("rainbow",     1,  { Colors.bgDark }, { Colors.rainbow(0.8f) }, { Colors.rainbow(0.55f) }, { Colors.bgLight }, { Colors.rainbow(1f) },  { Colors.rainbow(1f) },  Lootables.identity("divider/rainbow"),   0.25f,  DecorationType.BIG);


    override fun asString(): String {
        return id
    }

    fun translation(): Text {
        return FcText.translatable("lootables.rarity.$id")
    }

    object Colors {
        val bgDark = ColorHelper.Argb.getArgb(0xF0, 0x10, 0x00, 0x10)
        val bgLight = ColorHelper.Argb.getArgb(0xF0, 0x19, 0x00, 0x19)

        internal fun rainbow(b: Float): Int {
            val hue = (Util.getMeasuringTimeMs() % 2000L).toFloat() / 2000f
            return Color.HSBtoRGB(hue, (b + 2f) / 3f, b)
        }

        internal fun color(color: Int): Int {
            return color or (0xFF shl 24)
        }
    }

    enum class DecorationType(val hasDeco: Boolean) {
        NONE(false),
        SMALL(true),
        BIG(true)
    }

    internal companion object {
        internal val CODEC = StringIdentifiable.createCodec { entries.toTypedArray() }
        internal val KEYS = StringIdentifiable.toKeyable(entries.toTypedArray())

        private val INDEX_TO_VALUE: IntFunction<LootableRarity> = ValueLists.createIdToValueFunction(
            LootableRarity::ordinal, entries.toTypedArray(), ValueLists.OutOfBoundsHandling.ZERO
        )

        internal val PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, LootableRarity::ordinal)
    }
}