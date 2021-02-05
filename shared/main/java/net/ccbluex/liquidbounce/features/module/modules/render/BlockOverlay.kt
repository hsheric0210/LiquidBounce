/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.backend.Backend
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.BlockUtils.canBeClicked
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "BlockOverlay", description = "Allows you to change the design of the block overlay.", category = ModuleCategory.RENDER)
class BlockOverlay : Module()
{
	private val colorRedValue = IntegerValue("R", 68, 0, 255)
	private val colorGreenValue = IntegerValue("G", 117, 0, 255)
	private val colorBlueValue = IntegerValue("B", 255, 0, 255)
	private val colorAlphaValue = IntegerValue("Alpha", 102, 0, 255)

	private val colorRainbow = BoolValue("Rainbow", false)
	private val rainbowSpeedValue = IntegerValue("Rainbow-Speed", 10, 1, 10)
	private val saturationValue = FloatValue("HSB-Saturation", 1.0f, 0.0f, 1.0f)
	private val brightnessValue = FloatValue("HSB-Brightness", 1.0f, 0.0f, 1.0f)

	val infoValue = BoolValue("Info", false)

	val currentBlock: WBlockPos?
		get()
		{
			val blockPos = mc.objectMouseOver?.blockPos ?: return null

			if (canBeClicked(blockPos) && blockPos in mc.theWorld!!.worldBorder) return blockPos

			return null
		}

	@EventTarget
	fun onRender3D(event: Render3DEvent)
	{
		val theWorld = mc.theWorld ?: return

		val blockPos = currentBlock ?: return

		val block = theWorld.getBlockState(blockPos).block
		val partialTicks = event.partialTicks
		val alpha = colorAlphaValue.get()
		val rainbowSpeed = rainbowSpeedValue.get()
		val color = if (colorRainbow.get()) rainbow(alpha = alpha / 255.0F, speed = rainbowSpeed, saturation = saturationValue.get(), brightness = brightnessValue.get()) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), alpha)

		val glStateManager = classProvider.getGlStateManager()

		glStateManager.enableBlend()
		glStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
		RenderUtils.glColor(color)
		GL11.glLineWidth(2F)
		glStateManager.disableTexture2D()
		GL11.glDepthMask(false)

		@Suppress("ConstantConditionIf") if (Backend.MINECRAFT_VERSION_MINOR < 12) block.setBlockBoundsBasedOnState(theWorld, blockPos)

		val thePlayer = mc.thePlayer ?: return

		val x = thePlayer.lastTickPosX + (thePlayer.posX - thePlayer.lastTickPosX) * partialTicks
		val y = thePlayer.lastTickPosY + (thePlayer.posY - thePlayer.lastTickPosY) * partialTicks
		val z = thePlayer.lastTickPosZ + (thePlayer.posZ - thePlayer.lastTickPosZ) * partialTicks

		val boxExxpandSize = 0.0020000000949949026
		val axisAlignedBB = block.getSelectedBoundingBox(theWorld, theWorld.getBlockState(blockPos), blockPos).expand(boxExxpandSize, boxExxpandSize, boxExxpandSize).offset(-x, -y, -z)

		RenderUtils.drawSelectionBoundingBox(axisAlignedBB)
		RenderUtils.drawFilledBox(axisAlignedBB)
		GL11.glDepthMask(true)
		glStateManager.enableTexture2D()
		glStateManager.disableBlend()
		glStateManager.resetColor()
	}

	@EventTarget
	fun onRender2D(@Suppress("UNUSED_PARAMETER") event: Render2DEvent)
	{
		if (infoValue.get())
		{
			val blockPos = currentBlock ?: return
			val block = getBlock(blockPos) ?: return

			val info = "${block.localizedName} \u00A77ID: ${functions.getIdFromBlock(block)}"
			val scaledResolution = classProvider.createScaledResolution(mc)

			RenderUtils.drawBorderedRect(
				scaledResolution.scaledWidth / 2 - 2F, scaledResolution.scaledHeight / 2 + 5F, scaledResolution.scaledWidth / 2 + Fonts.font40.getStringWidth(info) + 2F, scaledResolution.scaledHeight / 2 + 16F, 3F, Color.BLACK.rgb, Color.BLACK.rgb
			)

			classProvider.getGlStateManager().resetColor()
			Fonts.font40.drawString(info, scaledResolution.scaledWidth / 2f, scaledResolution.scaledHeight / 2f + 7f, 0xffffff, false)
		}
	}
}
