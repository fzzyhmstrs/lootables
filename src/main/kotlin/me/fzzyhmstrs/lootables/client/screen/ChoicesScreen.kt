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

package me.fzzyhmstrs.lootables.client.screen

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.client.LootablesClientData
import me.fzzyhmstrs.lootables.config.ChoiceStyle
import me.fzzyhmstrs.lootables.config.LootablesConfig
import me.fzzyhmstrs.lootables.network.AbortChoicesC2SCustomPayload
import me.fzzyhmstrs.lootables.network.ChoicesS2CCustomPayload
import me.fzzyhmstrs.lootables.network.ChosenC2SCustomPayload
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import java.util.function.Supplier
import kotlin.math.ceil

class ChoicesScreen(private val choiceData: ChoicesS2CCustomPayload, private var choicesLeft: Int, internal val oldScreen: Screen?): Screen(FcText.empty()) {

    private val maxChoices = choicesLeft
    private val canClick: Supplier<Boolean> = Supplier { choicesLeft > 0 }
    private var widgets: List<ChoiceTileWidget> = listOf()
    private var confirmWidget = ConfirmChoicesWidget(
        0,
        0,
        { choicesLeft <= 0 },
        {
            if (choicesLeft > 1)
                "lootables.screen.choices_multiple".translate(choicesLeft)
            else if(choicesLeft == 1)
                "lootables.screen.choices_single".translate()
            else "lootables.screen.choices_ready".translate()
        }
    )

    override fun init() {
        super.init()
        if (widgets.isEmpty()) {
            val data = LootablesClientData.getData(choiceData.table, choiceData.choices)
            // stacked widget building
            val list: MutableList<ChoiceTileWidget> = mutableListOf()
            if (LootablesConfig.INSTANCE.tileStackingStyle.get() == ChoiceStyle.STACKED && data.size > 2) {
                val topRowCount = ceil(data.size.toFloat() / 2f).toInt()
                val bottomRowCount = data.size - topRowCount
                var widgetX = (width / 2) - (((topRowCount * LootablesConfig.INSTANCE.tileWidth) + ((topRowCount - 1) * 12)) / 2)
                var widgetY = (height / 2) - (((2 * LootablesConfig.INSTANCE.tileHeight) + 12 + 12 + 20) / 2)
                var dataIndex = 0
                for (i in 1..topRowCount) {
                    list.add(ChoiceTileWidget(
                        MinecraftClient.getInstance(),
                        data[dataIndex].id,
                        widgetX,
                        widgetY,
                        LootablesConfig.INSTANCE.tileWidth,
                        LootablesConfig.INSTANCE.tileHeight,
                        data[dataIndex].rarity,
                        data[dataIndex].display.provideIcons(),
                        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
                        canClick,
                        data[dataIndex].description,
                        dataIndex
                    ))
                    widgetX += LootablesConfig.INSTANCE.tileWidth
                    widgetX += 12
                    dataIndex++
                }
                widgetX = (width / 2) - (((bottomRowCount * LootablesConfig.INSTANCE.tileWidth) + ((bottomRowCount - 1) * 12)) / 2)
                widgetY += LootablesConfig.INSTANCE.tileHeight
                widgetY += 12
                for (i in 1.. bottomRowCount) {
                    list.add(ChoiceTileWidget(
                        MinecraftClient.getInstance(),
                        data[dataIndex].id,
                        widgetX,
                        widgetY,
                        LootablesConfig.INSTANCE.tileWidth,
                        LootablesConfig.INSTANCE.tileHeight,
                        data[dataIndex].rarity,
                        data[dataIndex].display.provideIcons(),
                        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
                        canClick,
                        data[dataIndex].description,
                        dataIndex
                    ))
                    widgetX += LootablesConfig.INSTANCE.tileWidth
                    widgetX += 12
                    dataIndex++
                }
                widgetX = (width / 2) - 55
                widgetY += LootablesConfig.INSTANCE.tileHeight
                widgetY += 12
                confirmWidget.setPosition(widgetX, widgetY)
                // end stacked widget building
            } else {
                // linear widget building
                var widgetX = (width / 2) - (((data.size * LootablesConfig.INSTANCE.tileWidth) + ((data.size - 1) * 12)) / 2)
                var widgetY = (height / 2) - ((LootablesConfig.INSTANCE.tileHeight + 12 + 20) / 2)
                for (i in data.indices) {
                    list.add(ChoiceTileWidget(
                        MinecraftClient.getInstance(),
                        data[i].id,
                        widgetX,
                        widgetY,
                        LootablesConfig.INSTANCE.tileWidth,
                        LootablesConfig.INSTANCE.tileHeight,
                        data[i].rarity,
                        data[i].display.provideIcons(),
                        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
                        canClick,
                        data[i].description,
                        i
                    ))
                    widgetX += LootablesConfig.INSTANCE.tileWidth
                    widgetX += 12
                }
                widgetX = (width / 2) - 55
                widgetY += LootablesConfig.INSTANCE.tileHeight
                widgetY += 12
                confirmWidget.setPosition(widgetX, widgetY)
                // end linear widget building
            }
            widgets = list
        } else {
            if (LootablesConfig.INSTANCE.tileStackingStyle.get() == ChoiceStyle.STACKED && widgets.size > 2) {
                val topRowCount = ceil(widgets.size.toFloat() / 2f).toInt()
                val bottomRowCount = widgets.size - topRowCount
                var widgetX = (width / 2) - (((topRowCount * LootablesConfig.INSTANCE.tileWidth) + ((topRowCount - 1) * 12)) / 2)
                var widgetY = (height / 2) - (((2 * LootablesConfig.INSTANCE.tileHeight) + 12 + 12 + 20) / 2)
                var listIndex = 0
                for (i in 1..topRowCount) {
                    widgets[listIndex].setPosition(widgetX, widgetY)
                    widgetX += LootablesConfig.INSTANCE.tileWidth
                    widgetX += 12
                    listIndex++
                }
                widgetX = (width / 2) - (((bottomRowCount * LootablesConfig.INSTANCE.tileWidth) + ((bottomRowCount - 1) * 12)) / 2)
                widgetY += LootablesConfig.INSTANCE.tileHeight
                widgetY += 12
                for (i in 1.. bottomRowCount) {
                    widgets[listIndex].setPosition(widgetX, widgetY)
                    widgetX += LootablesConfig.INSTANCE.tileWidth
                    widgetX += 12
                    listIndex++
                }
                widgetX = (width / 2) - 55
                widgetY += LootablesConfig.INSTANCE.tileHeight
                widgetY += 12
                confirmWidget.setPosition(widgetX, widgetY)
            } else {
                var widgetX = (width / 2) - (((widgets.size * LootablesConfig.INSTANCE.tileWidth) + ((widgets.size - 1) * 12)) / 2)
                var widgetY = (height / 2) - ((LootablesConfig.INSTANCE.tileHeight + 12 + 20) / 2)
                for (i in widgets.indices) {
                    widgets[i].setPosition(widgetX, widgetY)
                    widgetX += LootablesConfig.INSTANCE.tileWidth
                    widgetX += 12
                }
                widgetX = (width / 2) - 55
                widgetY += LootablesConfig.INSTANCE.tileHeight
                widgetY += 12
                confirmWidget.setPosition(widgetX, widgetY)
            }
        }
        addDrawableChild(confirmWidget)
        for (widget in widgets) {
            addDrawableChild(widget)
        }
    }

    override fun close() {
        ConfigApi.network().send(AbortChoicesC2SCustomPayload(choiceData.choiceKey), null)
        this.client?.setScreen(oldScreen)
    }

    private fun sendChosen() {
        ConfigApi.network().send(ChosenC2SCustomPayload(choiceData.table, choiceData.choiceKey, widgets.mapNotNull { it.id() }), null)
        this.client?.setScreen(oldScreen)
    }

    private inner class ConfirmChoicesWidget(i: Int, j: Int, private val isReady: Supplier<Boolean>, private val textSupplier: Supplier<Text>) : PressableWidget(i, j, 110, 20, textSupplier.get()) {

        override fun getMessage(): Text {
            return textSupplier.get()
        }

        override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
            appendDefaultNarrations(builder)
        }

        override fun onPress() {
            if (isReady.get()) {
                this@ChoicesScreen.sendChosen()
            }
        }
    }

}