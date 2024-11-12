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

package me.fzzyhmstrs.lootables_test.screen

import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.client.screen.ChoiceTileWidget
import me.fzzyhmstrs.lootables.config.LootablesConfig
import me.fzzyhmstrs.lootables.loot.LootableRarity
import me.fzzyhmstrs.lootables.loot.display.ExperienceLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.display.HealLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.display.ItemLootablePoolEntryDisplay
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import java.util.function.Supplier

class TestScreen(private var choicesLeft: Int = 1): Screen(FcText.empty()) {


    private val maxChoices = choicesLeft
    private val canClick: Supplier<Boolean> = Supplier { choicesLeft > 0 }
    private val display1 = ItemLootablePoolEntryDisplay(Items.DIAMOND_HELMET.defaultStack)
    private val display2 = HealLootablePoolEntryDisplay
    private val display3 = ExperienceLootablePoolEntryDisplay(true)

    private val confirmWidget = ConfirmChoicesWidget(
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

    private val choice1 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_1"),
        10, 10,
        LootablesConfig.INSTANCE.tileWidth,
        LootablesConfig.INSTANCE.tileHeight,
        LootableRarity.RAINBOW,
        { display1.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc1".translate(),
        0
    )

    private val choice2 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_2"),
        20 + LootablesConfig.INSTANCE.tileWidth, 10,
        LootablesConfig.INSTANCE.tileWidth,
        LootablesConfig.INSTANCE.tileHeight,
        LootableRarity.COMMON,
        { display2.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc2".translate(),
        0
    )

    private val choice3 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_3"),
        30 + LootablesConfig.INSTANCE.tileWidth * 2, 10,
        LootablesConfig.INSTANCE.tileWidth,
        LootablesConfig.INSTANCE.tileHeight,
        LootableRarity.RARE,
        { display3.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc3".translate(),
        0
    )

    override fun init() {
        super.init()
        confirmWidget.setPosition(this.width/2 - 55, LootablesConfig.INSTANCE.tileHeight + 20)
        addDrawableChild(confirmWidget)
        addDrawableChild(choice1)
        addDrawableChild(choice2)
        addDrawableChild(choice3)
    }

    private fun sendChosen() {
        println()
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
                this@TestScreen.sendChosen()
            }
        }
    }

}