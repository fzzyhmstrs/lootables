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

import me.fzzyhmstrs.fzzy_config.entry.Entry
import me.fzzyhmstrs.fzzy_config.entry.EntryValidator
import me.fzzyhmstrs.fzzy_config.screen.entry.Decorated
import me.fzzyhmstrs.fzzy_config.screen.widget.internal.CustomPressableWidget
import me.fzzyhmstrs.fzzy_config.util.FcText.descLit
import me.fzzyhmstrs.fzzy_config.util.FcText.transLit
import me.fzzyhmstrs.fzzy_config.validation.misc.ChoiceValidator
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

class ValidatedChoiceStyle: ValidatedEnum<ChoiceStyle>(ChoiceStyle.HORIZONTAL) {

    @Suppress("UnstableApiUsage")
    override fun widgetEntry(choicePredicate: ChoiceValidator<ChoiceStyle>): ClickableWidget {
        return ChoiceStylesOptionsWidget(choicePredicate, this)
    }

    //client
    private class ChoiceStylesOptionsWidget(choicePredicate: ChoiceValidator<ChoiceStyle>, private val entry: Entry<ChoiceStyle, *>): CustomPressableWidget(0, 0, 110, 20, entry.get().let { it.transLit(it.name) }), Decorated {

        private val constants = entry.get().declaringJavaClass.enumConstants.filter {
            choicePredicate.validateEntry(it, EntryValidator.ValidationType.STRONG).isValid()
        }

        init {
            entry.descLit("").takeIf { it.string != "" }?.let { tooltip = Tooltip.of(it) }
        }

        override fun getNarrationMessage(): MutableText {
            return entry.get().let { it.transLit(it.name) }
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
            appendDefaultNarrations(builder)
        }

        override fun onPress() {
            val newIndex = (constants.indexOf(entry.get()) + 1).takeIf { it < constants.size } ?: 0
            val newConst = constants[newIndex]
            message = newConst.let { it.transLit(it.name) }
            newConst.descLit("").takeIf { it.string != "" }?.also { tooltip = Tooltip.of(it) }
            entry.accept(newConst)
        }

        override fun decorationId(): Identifier {
            return entry.get().id
        }

    }

}