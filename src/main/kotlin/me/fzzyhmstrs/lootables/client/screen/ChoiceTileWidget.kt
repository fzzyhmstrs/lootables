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
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.input.KeyCodes
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
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
    private val id: Identifier,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val rarity: LootableRarity,
    private val icons: Supplier<List<TileIcon>>,
    private val choiceCallback: Consumer<Boolean>,
    private val canClick: Supplier<Boolean>,
    description: Text,
    delay: Float = 0f): ClickableWidget(x, y, if(width < 62) 62 else width, if (height < 39) 39 else height, rarity.translation())
{

    private val descriptions: List<OrderedText> = client.textRenderer.wrapLines(description, width - 6)
    private var clicked = false
    private var tooltips: List<OrderedText>? = null

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

    private val verticalPadding = 2

    private val iconStartY
        get() = y + 2 + verticalPadding

    private val dividerStartY
        get() = iconStartY + 18 + verticalPadding - 1

    private val textStartY
        get() = dividerStartY + 5 + verticalPadding - 1

    private val textEndY
        get() = y + height - 2 - verticalPadding

    private val availableTextHeight
        get() = textEndY - textStartY

    init {
        val textHeight = descriptions.size * 10 - 1
        if (availableTextHeight < textHeight)
            this.tooltips = client.textRenderer.wrapLines(description, 170)
    }

    fun id(): Identifier? {
        return if(clicked) id else null
    }

    private fun onPress(): Boolean {
        if (canClick.get() || clicked) {
            clicked = !clicked
            choiceCallback.accept(clicked)
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!this.active || !this.visible) return false
        if (!this.isValidClickButton(button)) return false
        if (!this.clicked(mouseX, mouseY)) return false
        if(this.onPress()) {
            this.isFocused = false
            this.playDownSound(MinecraftClient.getInstance().soundManager)
        }
        return clicked
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!this.active || !this.visible) return false
        if (!KeyCodes.isToggle(keyCode)) return false
        if(this.onPress()) {
            this.playDownSound(MinecraftClient.getInstance().soundManager)
        }
        return true
    }

    override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val hovered = (this.isSelected && canClick.get()) || this.clicked
        renderTile(context, x, y, hovered, this.clicked)

        //tooltips
        if (!canClick.get() && !clicked) {
            if (this.isFocused && MinecraftClient.getInstance().navigationType.isKeyboard) {
                val l = tooltips?.let { listOf(FcText.translatable("lootables.screen.no_more_choices").asOrderedText()) + it } ?: listOf(FcText.translatable("lootables.screen.no_more_choices").asOrderedText())
                MinecraftClient.getInstance().currentScreen?.setTooltip(l, FocusedTooltipPositioner(ScreenRect(x, y, width, height)), this.isFocused)
            } else if (this.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && !MinecraftClient.getInstance().navigationType.isKeyboard) {
                val l = tooltips?.let { listOf(FcText.translatable("lootables.screen.no_more_choices").asOrderedText()) + it } ?: listOf(FcText.translatable("lootables.screen.no_more_choices").asOrderedText())
                MinecraftClient.getInstance().currentScreen?.setTooltip(l, HoveredTooltipPositioner.INSTANCE, true)
            }
        } else if (tooltips?.isNotEmpty() == true) {
            if (this.isFocused && MinecraftClient.getInstance().navigationType.isKeyboard) {
                MinecraftClient.getInstance().currentScreen?.setTooltip(tooltips, FocusedTooltipPositioner(ScreenRect(x, y, width, height)), this.isFocused)
            } else if (this.isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && !MinecraftClient.getInstance().navigationType.isKeyboard) {
                MinecraftClient.getInstance().currentScreen?.setTooltip(tooltips, HoveredTooltipPositioner.INSTANCE, true)
            }
        }
    }

    private fun renderTile(context: DrawContext, x: Int, y: Int, hovered: Boolean, clicked: Boolean) {
        val time = Util.getMeasuringTimeMs()
        if (!easeInAnimator.shouldRender(time)) return
        context.matrices.push()
        context.matrices.translate(0f, easeInAnimator.offsetY(time), 0f)

        //background
        val backgroundColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, rarity.bgColor.get(), hovered), rarity.bgHoveredColor.get(), hovered)
        if (!clicked) {
            renderHorizontalLine(context, x, y - 1, width, backgroundColor)
            renderHorizontalLine(context, x, y + height, width, backgroundColor)
            renderRectangle(context, x, y, width, height, backgroundColor)
            renderVerticalLine(context, x - 1, y, height, backgroundColor)
            renderVerticalLine(context, x + width, y, height, backgroundColor)
        } else {
            renderHorizontalLine(context, x - 3, y - 4, width + 6, backgroundColor)
            renderHorizontalLine(context, x - 3, y + height + 3, width + 6, backgroundColor)
            renderRectangle(context, x - 3, y - 3, width + 6, height + 6, backgroundColor)
            renderVerticalLine(context, x - 4, y - 3, height + 6, backgroundColor)
            renderVerticalLine(context, x + width + 3, y - 3, height + 6, backgroundColor)
        }

        val startColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, rarity.startColor.get(), hovered), rarity.startHoveredColor.get(), hovered)
        val endColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, rarity.endColor.get(), hovered), rarity.endHoveredColor.get(), hovered)
        renderBorder(context, x, y + 1, width, height, startColor, endColor)

        //gradient
        if (rarity.gradientOpacity > 0f) {
            val h = ((textStartY + textEndY) / 2) - y
            renderGradient(context, x, y, width, h, ColorHelper.Argb.withAlpha((0xFF * rarity.gradientOpacity).toInt(), startColor) , ColorHelper.Argb.withAlpha(0, backgroundColor))
        }

        //description
        val textHeight = descriptions.size * 10 - 1
        val textColor = hoveredAnimator.lerp(time, easeInAnimator.lerpInternal(time, 0xC4C4C4, hovered), 0xFFFFFF, hovered)
        if (availableTextHeight < textHeight) {
            val heightDiff = textHeight - availableTextHeight
            val seconds = time / 1000.0;
            val rate = max(heightDiff * 0.5, 4.5)
            val offset = if(hovered) sin(1.5707963267948966 * cos(6.283185307179586 * seconds / rate)) / 2.0 + 0.5 else 0.0
            val position = MathHelper.lerp(offset, 0.0, heightDiff.toDouble())
            var linePosition = textStartY - position.toInt()
            context.enableScissor(x + 3, textStartY, x + width - 3, textEndY)
                for (line in descriptions) {
                    if (linePosition < (textStartY - 10)) {
                        linePosition += 10
                        continue
                    }
                    if (linePosition > textEndY) {
                        break
                    }
                    //context.drawTextWithShadow(client.textRenderer, line, x + 3, linePosition, textColor)
                    context.drawCenteredTextWithShadow(client.textRenderer, line, x + width/2, linePosition, textColor)
                    linePosition += 10
                }
            context.disableScissor()
        } else {
            var linePosition = ((textEndY + textStartY) / 2) - (textHeight / 2)
            for (line in descriptions) {
                //context.drawTextWithShadow(client.textRenderer, line, x + 3, linePosition, textColor)
                context.drawCenteredTextWithShadow(client.textRenderer, line, x + width/2, linePosition, textColor)
                linePosition += 10
            }
        }

        //icons
        val iconsList = icons.get()
        var iconStartPoint = ((x + (x + width)) / 2) - (((iconsList.size * 18) + ((iconsList.size - 1) * 1)) / 2)
        for (icon in iconsList) {
            icon.render(context, iconStartPoint, iconStartY)
            iconStartPoint += 19
        }

        // divider
        val dividerStartPoint = ((x + (x + width)) / 2) - 28
        context.drawTex(rarity.dividerId, dividerStartPoint, dividerStartY, 56, 5, startColor)
        /*if (width >= 68) {
            renderHorizontalLine(context, x + 2, y + 3 + 18 + 2, dividerStartPoint - (x + 3), 0, startColor)
            renderHorizontalLine(context, dividerStartPoint + 57, y + 3 + 18 + 2, dividerStartPoint - (x + 3), 0, startColor)
        }*/

        //hovered corners
        if (hovered && !clicked) {
            renderCorners(context, x - 4, y - 3, width + 8, height + 8, startColor, endColor, backgroundColor)
        }

        // outer border
        if (clicked) {
            renderBorder(context, x - 3, y - 2, width + 6, height + 6, startColor, endColor)
        }

        //decorations
        if (rarity.drawDecoration) {
            renderCorners(context, x + 2, y + 3, width - 4, height - 4, startColor, endColor, backgroundColor)
        }

        context.matrices.pop()
    }

    private fun renderBorder(context: DrawContext, x: Int, y: Int, width: Int, height: Int, startColor: Int, endColor: Int) {
        val x1 = (x + width) - 1
        val h1 = height - 2
        val y1 = y - 1
        val y2 = y - 1 + height - 1

        renderVerticalLine(context, x, y, h1, startColor, endColor)
        renderVerticalLine(context, x1, y, h1, startColor, endColor)
        renderHorizontalLine(context, x, y1, width, startColor)
        renderHorizontalLine(context, x, y2, width, endColor)
    }

    private fun renderGradient(context: DrawContext, x: Int, y: Int, width: Int, height: Int, startColor: Int, endColor: Int) {
        context.fillGradient(x, y, x + width, y + height, 0, startColor, endColor)
    }

    private fun renderCorners(context: DrawContext, x: Int, y: Int, width: Int, height: Int, startColor: Int, endColor: Int, backgroundColor: Int) {
        val x1 = x + width - 1
        val x2 = x + width - 10
        val h1 = 10
        val w1 = 10
        val y1 = y + height - 11
        val y2 = y - 1
        val y3 = y - 1 + height - 1
        val startColor1 = ColorHelper.Argb.lerp((height - 10)/height.toFloat(), startColor, endColor)
        val endColor1 = ColorHelper.Argb.lerp(10/height.toFloat(), startColor, endColor)

        context.fill(x - 1, y2, x + 2, y + 10, 0, backgroundColor)
        context.fill(x, y + 10, x + 1, y + 11, 0, backgroundColor)
        context.fill(x, y2 - 1, x + 10, y2, 0, backgroundColor)
        context.fill(x + 2, y, x + 10, y + 1, 0, backgroundColor)
        context.fill(x + 10, y2, x + 11, y2 + 1, 0, backgroundColor)

        context.fill(x - 1, y1, x + 2, y1 + 10, 0, backgroundColor)
        context.fill(x, y1 - 1, x + 1, y1, 0, backgroundColor)
        context.fill(x, y3 + 1, x + 10, y3 + 2, 0, backgroundColor)
        context.fill(x + 2, y3 - 1, x + 10, y3, 0, backgroundColor)
        context.fill(x + 10, y3, x + 11, y3 + 1, 0, backgroundColor)

        context.fill(x1 - 1, y2, x1 + 2, y + 10, 0, backgroundColor)
        context.fill(x1, y + 10, x1 + 1, y + 11, 0, backgroundColor)
        context.fill(x1 - 9, y2 - 1, x1 + 1, y2, 0, backgroundColor)
        context.fill(x1 - 9, y, x1 - 1, y + 1, 0, backgroundColor)
        context.fill(x1 - 10, y2, x1 - 9, y2 + 1, 0, backgroundColor)

        context.fill(x1 - 1, y1, x1 + 2, y1 + 10, 0, backgroundColor)
        context.fill(x1, y1 - 1, x1 + 1, y1, 0, backgroundColor)
        context.fill(x1 - 9, y1 + 10, x1 + 1, y1 + 11, 0, backgroundColor)
        context.fill(x1 - 9, y1 + 8, x1 - 1, y1 + 9, 0, backgroundColor)
        context.fill(x1 - 10, y1 + 9, x1 - 9, y1 + 10, 0, backgroundColor)


        renderVerticalLine(context,   x,    y,  h1, startColor,  endColor1)
        renderVerticalLine(context,   x,    y1, h1, startColor1, endColor)
        renderVerticalLine(context,   x1,   y,  h1, startColor,  endColor1)
        renderVerticalLine(context,   x1,   y1, h1, startColor1, endColor)
        renderHorizontalLine(context, x,    y2, w1, startColor)
        renderHorizontalLine(context, x2,   y2, w1, startColor)
        renderHorizontalLine(context, x,    y3, w1,              endColor)
        renderHorizontalLine(context, x2,   y3, w1,              endColor)
    }

    private fun renderVerticalLine(context: DrawContext, x: Int, y: Int, height: Int, color: Int) {
        context.fill(x, y, x + 1, y + height, 0, color)
    }

    private fun renderVerticalLine(context: DrawContext, x: Int, y: Int, height: Int, startColor: Int, endColor: Int) {
        context.fillGradient(x, y, x + 1, y + height, 0, startColor, endColor)
    }

    private fun renderHorizontalLine(context: DrawContext, x: Int, y: Int, width: Int, color: Int) {
        context.fill(x, y, x + width, y + 1, 0, color)
    }

    private fun renderRectangle(context: DrawContext, x: Int, y: Int, width: Int, height: Int, color: Int) {
        context.fill(x, y, x + width, y + height, 0, color)
    }

    override fun setWidth(width: Int) {
        this.width = max(width, 62)
    }

    override fun setHeight(height: Int) {
        this.height = max(height, 39)
    }

    private fun createTooltipString(tooltip: List<OrderedText>?): String {
        val tt = tooltip ?: return ""
        val builder = StringBuilder()
        for (tip in tt) {
            tip.accept { _, _, codepoint ->
                builder.appendCodePoint(codepoint)
                true
            }
        }
        return builder.toString()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        appendDefaultNarrations(builder)
        val str = createTooltipString(descriptions)
        if (str.isEmpty()) return
        builder.put(NarrationPart.HINT, str)
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

    private inner class EaseIn(d: Float): Animator {

        private val delay: Int = (d * 50).toInt()

        private val startTime: Long by lazy {
            Util.getMeasuringTimeMs()
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
            return ((time - startTime) - delay) >= 0L
        }
    }

    private inner class Hover: Animator {

        private var lastHoverTime: Long = 0L
        private var lastFrameTime: Long = 0L
        private var lastHovered: Boolean? = null

        private var progress: Float = 0f

        private val duration: Int = (LootablesConfig.INSTANCE.hoverDelay * 50)

        private fun progress(time: Long, hovered: Boolean): Float {
            if (lastHovered != hovered) {
                lastHoverTime = time
                lastHovered = hovered
            }
            if (lastFrameTime == 0L) {
                lastFrameTime = time
            }
            val increment = (if(hovered) 1f else -0.7f) * ((time - lastFrameTime).toFloat() / duration.toFloat())
            progress = MathHelper.clamp(progress + increment, 0f, 1f)
            //if (hovered) println("time: $time, lastFrameTime: $lastFrameTime, progress: $progress, increment: $increment, nanoTime: ${System.nanoTime()}")
            lastFrameTime = time
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
}
