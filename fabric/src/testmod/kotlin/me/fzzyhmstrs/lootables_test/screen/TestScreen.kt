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
import me.fzzyhmstrs.lootables.loot.display.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import java.util.function.Supplier

class TestScreen(private var choicesLeft: Int = 1): Screen(FcText.empty()) {


    private val maxChoices = choicesLeft
    private val canClick: Supplier<Boolean> = Supplier { choicesLeft > 0 }
    private val display1 = ItemLootablePoolEntryDisplay(Items.DIAMOND_HELMET.defaultStack, false)
    private val display2 = AttributeLootablePoolEntryDisplay(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0f, EntityAttributeModifier.Operation.ADD_VALUE, true)
    private val display3 = TableLootablePoolEntryDisplay(listOf(Items.DIAMOND.defaultStack, Items.GOLD_INGOT.defaultStack, Items.GLOW_BERRIES.defaultStack), false)
    private val display4 = MultiLootablePoolEntryDisplay(
        StatusEffectLootablePoolEntryDisplay(StatusEffects.JUMP_BOOST, 1, 100),
        StatusEffectLootablePoolEntryDisplay(StatusEffects.STRENGTH, 1, 100),
        StatusEffectLootablePoolEntryDisplay(StatusEffects.LUCK, 1, 100)
    )
    private val display5 = ExperienceLootablePoolEntryDisplay("5", false)
    private val display6 =  HealLootablePoolEntryDisplay("3")


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
        LootablesConfig.INSTANCE.tileWidth.get(),
        LootablesConfig.INSTANCE.tileHeight.get(),
        LootableRarity.RAINBOW,
        { display1.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc1".translate(),
        0f
    )

    private val choice2 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_2"),
        20 + LootablesConfig.INSTANCE.tileWidth.get(), 10,
        LootablesConfig.INSTANCE.tileWidth.get(),
        LootablesConfig.INSTANCE.tileHeight.get(),
        LootableRarity.LEGENDARY,
        { display2.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc2".translate(),
        1.2f
    )

    private val choice3 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_3"),
        30 + LootablesConfig.INSTANCE.tileWidth.get() * 2, 10,
        LootablesConfig.INSTANCE.tileWidth.get(),
        LootablesConfig.INSTANCE.tileHeight.get(),
        LootableRarity.EPIC,
        { display3.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc3".translate(),
        2.4f
    )

    private val choice4 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_4"),
        40 + LootablesConfig.INSTANCE.tileWidth.get() * 3, 10,
        LootablesConfig.INSTANCE.tileWidth.get(),
        LootablesConfig.INSTANCE.tileHeight.get(),
        LootableRarity.RARE,
        { display4.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc4".translate(),
        3.6f
    )

    private val choice5 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_5"),
        10, 20 + LootablesConfig.INSTANCE.tileHeight.get(),
        LootablesConfig.INSTANCE.tileWidth.get(),
        LootablesConfig.INSTANCE.tileHeight.get(),
        LootableRarity.UNCOMMON,
        { display5.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc5".translate(),
        4.8f
    )

    private val choice6 = ChoiceTileWidget(
        MinecraftClient.getInstance(),
        Identifier.of("lootables_test", "choice_6"),
        20 + LootablesConfig.INSTANCE.tileWidth.get(), 20 + LootablesConfig.INSTANCE.tileHeight.get(),
        LootablesConfig.INSTANCE.tileWidth.get(),
        LootablesConfig.INSTANCE.tileHeight.get(),
        LootableRarity.COMMON,
        { display6.provideIcons() },
        { choice -> MathHelper.clamp(if(choice) choicesLeft-- else choicesLeft++, 0, maxChoices) },
        canClick,
        "lootables_test.test_screen.desc6".translate(),
        6f
    )

    override fun init() {
        super.init()
        confirmWidget.setPosition(this.width/2 - 55, (LootablesConfig.INSTANCE.tileHeight.get() * 2) + 30)
        addDrawableChild(confirmWidget)
        addDrawableChild(choice1)
        addDrawableChild(choice2)
        addDrawableChild(choice3)
        addDrawableChild(choice4)
        addDrawableChild(choice5)
        addDrawableChild(choice6)
    }

    override fun shouldPause(): Boolean {
        return false
    }

    private fun sendChosen() {
        println(choice1.id())
        println(choice2.id())
        println(choice3.id())
        println(choice4.id())
        println(choice5.id())
        println(choice6.id())
        this.close()
    }

    private inner class ConfirmChoicesWidget(i: Int, j: Int, private val isReady: Supplier<Boolean>, private val textSupplier: Supplier<Text>) : PressableWidget(i, j, 110, 20, textSupplier.get()) {

        override fun getMessage(): Text {
            return textSupplier.get()
        }

        override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            this.active = isReady.get()
            super.renderWidget(context, mouseX, mouseY, delta)
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