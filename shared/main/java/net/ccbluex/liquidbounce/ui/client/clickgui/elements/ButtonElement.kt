/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
open class ButtonElement(displayName: String) : Element()
{
	open lateinit var displayName: String
		protected set
	var color = 0xffffff
	var hoverTime = 0

	open fun createButton(displayName: String)
	{
		this.displayName = displayName
	}

	override fun drawScreen(mouseX: Int, mouseY: Int, button: Float)
	{
		LiquidBounce.clickGui.style.drawButtonElement(mouseX, mouseY, this)
		super.drawScreen(mouseX, mouseY, button)
	}

	override var height: Int
		get() = 16
		set(height)
		{
			super.height = height
		}

	fun isHovering(mouseX: Int, mouseY: Int): Boolean = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16

	init
	{
		@Suppress("LeakingThis") createButton(displayName)
	}
}
