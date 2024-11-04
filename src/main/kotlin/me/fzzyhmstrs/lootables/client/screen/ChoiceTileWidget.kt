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

import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.fzzy_config.util.RenderUtil.drawTex
import me.fzzyhmstrs.lootables.config.LootablesConfig
import me.fzzyhmstrs.lootables.loot.LootableRarity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.input.KeyCodes
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Util
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.MathHelper
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class ChoiceTileWidget(
    private val client: MinecraftClient,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val rarity: LootableRarity,
    private val icons: List<TileIcon>,
    private val choiceCallback: Consumer<Boolean>,
    private val canClick: Supplier<Boolean>,
    description: Text,
    delay: Int = 0): ClickableWidget(x, y, if(width < 62) 62 else width, if (height < 39) 39 else height, FcText.empty())
{

    private val descriptions: List<OrderedText> = client.textRenderer.wrapLines(description, width - 6)
    private var clicked = false

    private var easeInAnimator: Animator = if(LootablesConfig.INSTANCE.easeInTiles && LootablesConfig.INSTANCE.easeInDuration > 0) {
        EaseIn(delay)
    } else {
        Static
    }

    private val hoveredAnimator: Animator = if (LootablesConfig.INSTANCE.animateTileHover && LootablesConfig.INSTANCE.hoverDelay > 0) {
        Hover()
    } else {
        Static
    }

    private fun onPress() {
        if (canClick.get() || clicked) {
            clicked = !clicked
            choiceCallback.accept(clicked)
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        this.onPress()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!this.active || !this.visible) {
            return false
        } else if (KeyCodes.isToggle(keyCode)) {
            this.playDownSound(MinecraftClient.getInstance().soundManager)
            this.onPress()
            return true
        } else {
            return false
        }
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderTile(context, x, y, (this.isSelected && canClick.get()) || this.clicked, this.clicked)
    }

    private fun renderTile(context: DrawContext, x: Int, y: Int, hovered: Boolean, clicked: Boolean) {
        val time = Util.getMeasuringTimeMs()
        if (!easeInAnimator.shouldRender(time)) return
        context.matrices.push()
        context.matrices.translate(0f, easeInAnimator.offsetY(time), 0f)

        //background
        val backgroundColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, rarity.bgColor.get(), hovered), rarity.bgHoveredColor.get(), hovered)
        renderHorizontalLine(context, x, y - 1, width, 0, backgroundColor)
        renderHorizontalLine(context, x, y + height, width, 0, backgroundColor)
        renderRectangle(context, x, y, width, height, 0, backgroundColor)
        renderVerticalLine(context, x - 1, y, height, 0, backgroundColor)
        renderVerticalLine(context, x + width, y, height, 0, backgroundColor)

        val startColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, rarity.startColor.get(), hovered), rarity.startHoveredColor.get(), hovered)
        val endColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, rarity.endColor.get(), hovered), rarity.endHoveredColor.get(), hovered)
        renderBorder(context, x, y, width, height, 0, startColor, endColor)

        //description
        val textHeight = descriptions.size * 10 - 1
        val availableTextHeight = (y + height - 3) - (y + 3 + 18 + 5)
        val textColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, 0xC4C4C4, hovered), 0xFFFFFF, hovered)
        if (availableTextHeight < textHeight) {
            val heightDiff = textHeight - availableTextHeight
            val seconds = time / 1000.0;
            val rate = max(heightDiff * 0.5, 3.0)
            val offset = sin(1.5707963267948966 * cos(6.283185307179586 * seconds / rate)) / 2.0 + 0.5
            val position = MathHelper.lerp(offset, 0.0, heightDiff.toDouble())
            var linePosition = (((y + height - 3) + (y + 3 + 18 + 5)) / 2) - 4 - position.toInt()
            context.enableScissor(x + 3, y + 3 + 18 + 5, x + width - 3, y + height - 3)
                for (line in descriptions) {
                    context.drawTextWithShadow(client.textRenderer, line, x + 3, linePosition, textColor)
                    linePosition += 10
                }
            context.disableScissor()
        } else {
            var linePosition = (((y + height - 3) + (y + 3 + 18 + 5)) / 2) - 4
            for (line in descriptions) {
                context.drawTextWithShadow(client.textRenderer, line, x + 3, linePosition, textColor)
                linePosition += 10
            }
        }

        //icons
        var iconStartPoint = ((x + (x + width)) / 2) - (((icons.size * 18) + ((icons.size - 1) * 1)) / 2)
        for (icon in icons) {
            icon.render(context, iconStartPoint, y + 3)
            iconStartPoint += 19
        }

        // divider
        val dividerStartPoint = ((x + (x + width)) / 2) - 28
        context.drawTex(rarity.dividerId, dividerStartPoint, y + 3 + 18, 56, 5, startColor)

        if (width >= 68) {
            renderHorizontalLine(context, x + 3, y + 3 + 18 + 2, dividerStartPoint - x + 2, 0, startColor)
            renderHorizontalLine(context, dividerStartPoint + 57, y + 3 + 18 + 2, dividerStartPoint - x + 2, 0, startColor)
        }

        // outer border
        if (clicked) {
            renderBorder(context, x - 3, y - 3, width + 6, height + 6, 0, startColor, endColor)
        }

        context.matrices.pop()
    }

    private fun renderBorder(context: DrawContext, x: Int, y: Int, width: Int, height: Int, z: Int, startColor: Int, endColor: Int) {
        renderVerticalLine(context, x, y, height - 2, z, startColor, endColor)
        renderVerticalLine(context, x + width - 1, y, height - 2, z, startColor, endColor)
        renderHorizontalLine(context, x, y - 1, width, z, startColor)
        renderHorizontalLine(context, x, y - 1 + height - 1, width, z, endColor)
    }

    private fun renderVerticalLine(context: DrawContext, x: Int, y: Int, height: Int, z: Int, color: Int) {
        context.fill(x, y, x + 1, y + height, z, color)
    }

    private fun renderVerticalLine(context: DrawContext, x: Int, y: Int, height: Int, z: Int, startColor: Int, endColor: Int) {
        context.fillGradient(x, y, x + 1, y + height, z, startColor, endColor)
    }

    private fun renderHorizontalLine(context: DrawContext, x: Int, y: Int, width: Int, z: Int, color: Int) {
        context.fill(x, y, x + width, y + 1, z, color)
    }

    private fun renderRectangle(context: DrawContext, x: Int, y: Int, width: Int, height: Int, z: Int, color: Int) {
        context.fill(x, y, x + width, y + height, z, color)
    }

    override fun setWidth(width: Int) {
        this.width = max(width, 62)
    }

    override fun setHeight(height: Int) {
        this.height = max(height, 39)
    }

    private interface Animator {
        fun offsetY(time: Long): Float
        fun lerpInternal(time: Long, color: Int, hovered: Boolean): Int
        fun lerp(time: Long, startColor: Int, endColor: Int, hovered: Boolean): Int
        fun shouldRender(time: Long): Boolean
    }

    private object Static: Animator {

        override fun offsetY(time: Long): Float {
            return 0f
        }

        override fun lerpInternal(time: Long, color: Int, hovered: Boolean): Int {
            return color
        }

        override fun lerp(time: Long, startColor: Int, endColor: Int, hovered: Boolean): Int {
            return startColor
        }

        override fun shouldRender(time: Long): Boolean {
            return true
        }
    }

    private inner class EaseIn(private val delay: Int): Animator {

        private val startTime: Long by lazy {
            System.currentTimeMillis()
        }

        private val end: Int = delay + (LootablesConfig.INSTANCE.easeInDuration * 50)
        private val duration: Float = (LootablesConfig.INSTANCE.easeInDuration * 50).toFloat()

        private fun progress(time: Long): Float {
            val timeSinceStart = ((time - startTime) - delay).toInt()
            return MathHelper.clamp(timeSinceStart, 0, end) / duration
        }

        override fun offsetY(time: Long): Float {
            val progress = progress(time)
            if (progress >= 0.9999f) {
                this@ChoiceTileWidget.easeInAnimator = Static
            }
            return ((progress * progress) - (2f * progress) + 1f) * LootablesConfig.INSTANCE.easeInAmount
        }

        override fun lerpInternal(time: Long, color: Int, hovered: Boolean): Int {
            val progress = progress(time)
            if (progress >= 0.9999f) {
                this@ChoiceTileWidget.easeInAnimator = Static
            }
            return ColorHelper.Argb.lerp(progress, ColorHelper.Argb.withAlpha(0x0F, color), color)
        }

        override fun lerp(time: Long, startColor: Int, endColor: Int, hovered: Boolean): Int {
            val progress = progress(time)
            if (progress >= 0.9999f) {
                this@ChoiceTileWidget.easeInAnimator = Static
            }
            return ColorHelper.Argb.lerp(progress, ColorHelper.Argb.withAlpha(0x0F, endColor), endColor)
        }

        override fun shouldRender(time: Long): Boolean {
            return ((time - startTime) - delay).toInt() >= 0
        }
    }

    private inner class Hover: Animator {

        private var lastHoverTime: Long = 0L

        private var progress: Float = 0f

        private val duration: Int = (LootablesConfig.INSTANCE.hoverDelay * 50)

        private fun progress(time: Long, hovered: Boolean): Float {
            if ((time - lastHoverTime) >= duration) lastHoverTime = time
            val increment = (if(hovered) 1.5f else -1f) * ((time - lastHoverTime) / duration)
            lastHoverTime = time
            progress = MathHelper.clamp(progress + increment, 0f, 1f)
            return progress
        }

        override fun offsetY(time: Long): Float {
            return 0f
        }

        override fun lerpInternal(time: Long, color: Int, hovered: Boolean): Int {
            val progress = progress(time, hovered)
            return ColorHelper.Argb.lerp(progress, color, color)
        }

        override fun lerp(time: Long, startColor: Int, endColor: Int, hovered: Boolean): Int {
            val progress = progress(time, hovered)
            return ColorHelper.Argb.lerp(progress, startColor, endColor)
        }

        override fun shouldRender(time: Long): Boolean {
            return true
        }
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        TODO("Not yet implemented")
    }
}