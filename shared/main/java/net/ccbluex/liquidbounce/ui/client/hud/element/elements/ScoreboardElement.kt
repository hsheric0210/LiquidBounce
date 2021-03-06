/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.api.minecraft.scoreboard.IScoreObjective
import net.ccbluex.liquidbounce.api.minecraft.util.WEnumChatFormatting
import net.ccbluex.liquidbounce.features.module.modules.render.NoScoreboard
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.createRGB
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.opengl.GL11
import kotlin.math.min

/**
 * CustomHUD scoreboard
 *
 * Allows to move and customize minecraft scoreboard
 */
@ElementInfo(name = "Scoreboard", force = true)
class ScoreboardElement(x: Double = 5.0, y: Double = 0.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.MIDDLE)) : Element(x, y, scale, side)
{

	private val textRedValue = IntegerValue("Text-R", 255, 0, 255)
	private val textGreenValue = IntegerValue("Text-G", 255, 0, 255)
	private val textBlueValue = IntegerValue("Text-B", 255, 0, 255)

	private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
	private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
	private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
	private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 95, 0, 255)

	private val rectValue = BoolValue("Rect", false)
	private val rectColorModeValue = ListValue("Rect-Color", arrayOf("Custom", "Rainbow"), "Custom")
	private val rectColorRedValue = IntegerValue("Rect-R", 0, 0, 255)
	private val rectColorGreenValue = IntegerValue("Rect-G", 111, 0, 255)
	private val rectColorBlueValue = IntegerValue("Rect-B", 255, 0, 255)
	private val rectColorBlueAlpha = IntegerValue("Rect-Alpha", 255, 0, 255)

	private val saturationValue = FloatValue("HSB-Saturation", 0.9f, 0f, 1f)
	private val brightnessValue = FloatValue("HSB-Brightness", 1f, 0f, 1f)

	private val rainbowSpeedValue = IntegerValue("Rainbow-Speed", 10, 1, 10)

	private val shadowValue = BoolValue("Shadow", false)
	private val fontValue = FontValue("Font", Fonts.minecraftFont)

	/**
	 * Draw element
	 */
	override fun drawElement(): Border?
	{
		if (NoScoreboard.state) return null

		val fontRenderer = fontValue.get()
		val fontHeight = fontRenderer.fontHeight

		val textColor = createRGB(textRedValue.get(), textGreenValue.get(), textBlueValue.get(), 255)
		val backColor = createRGB(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get(), backgroundColorAlphaValue.get())

		val rectColorMode = rectColorModeValue.get()
		val rectCustomColor = createRGB(rectColorRedValue.get(), rectColorGreenValue.get(), rectColorBlueValue.get(), rectColorBlueAlpha.get())

		val saturation = saturationValue.get()
		val brightness = brightnessValue.get()
		val rainbowSpeed = rainbowSpeedValue.get()

		val worldScoreboard = (mc.theWorld ?: return null).scoreboard
		var currObjective: IScoreObjective? = null
		val playerTeam = worldScoreboard.getPlayersTeam((mc.thePlayer ?: return null).name)

		if (playerTeam != null)
		{
			val colorIndex = playerTeam.chatFormat.colorIndex

			if (colorIndex >= 0) currObjective = worldScoreboard.getObjectiveInDisplaySlot(3 + colorIndex)
		}

		val objective = currObjective ?: worldScoreboard.getObjectiveInDisplaySlot(1) ?: return null

		val scoreboard = objective.scoreboard

		var scoreCollection = scoreboard.getSortedScores(objective)
		val scoreCollectionSize = scoreCollection.size

		val scores = scoreCollection.filter { !it.playerName.startsWith("#") }
		val scoresSize = scores.size

		scoreCollection = if (scoresSize > 15) scores.subList(min(scoresSize, scoreCollectionSize - 15), scoresSize) else scores

		var maxWidth = fontRenderer.getStringWidth(objective.displayName)

		val func = functions

		scoreCollection.map { score ->
			val playerName = score.playerName
			"${func.scoreboardFormatPlayerName(scoreboard.getPlayersTeam(playerName), playerName)}: ${WEnumChatFormatting.RED}${score.scorePoints}"
		}.forEach { maxWidth = maxWidth.coerceAtLeast(fontRenderer.getStringWidth(it)) }

		val maxHeight = scoreCollectionSize * fontHeight
		val backgroundXPos = -maxWidth - 3 - if (rectValue.get()) 3 else 0

		RenderUtils.drawRect(backgroundXPos - 2, -2, 5, (maxHeight + fontHeight), backColor)

		scoreCollection.forEachIndexed { index, score ->
			val playerName = score.playerName
			val team = scoreboard.getPlayersTeam(playerName)

			val formattedPlayerName = func.scoreboardFormatPlayerName(team, playerName)
			val scorePoints = "${WEnumChatFormatting.RED}${score.scorePoints}"

			val width = 5 - if (rectValue.get()) 4 else 0
			val height = maxHeight - index * fontHeight.toFloat()

			RenderUtils.resetColor()

			fontRenderer.drawString(formattedPlayerName, backgroundXPos.toFloat(), height, textColor, shadowValue.get())
			fontRenderer.drawString(scorePoints, (width - fontRenderer.getStringWidth(scorePoints)).toFloat(), height, textColor, shadowValue.get())

			if (index == scoreCollectionSize - 1)
			{
				val displayName = objective.displayName

				RenderUtils.resetColor()

				fontRenderer.drawString(displayName, (backgroundXPos + (maxWidth shr 1) - (fontRenderer.getStringWidth(displayName) shr 1)).toFloat(), (height - fontHeight), textColor, shadowValue.get())
			}

			if (rectValue.get())
			{
				val rectColor = when
				{
					rectColorMode.equals("Rainbow", ignoreCase = true) -> ColorUtils.rainbowRGB(offset = 400000000L * index, speed = rainbowSpeed, saturation = saturation, brightness = brightness)
					else -> rectCustomColor
				}

				RenderUtils.drawRect(2F, if (index == scoreCollectionSize - 1) -2F else height, 5F, if (index == 0) fontHeight.toFloat() else height + fontHeight * 2F, rectColor)
			}
		}

		return Border(-maxWidth - 5.0f - if (rectValue.get()) 3 else 0, -2F, 5F, maxHeight + fontHeight.toFloat())
	}
}
