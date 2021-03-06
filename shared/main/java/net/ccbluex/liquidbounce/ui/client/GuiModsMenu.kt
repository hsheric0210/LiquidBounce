/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiButton
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiScreen
import net.ccbluex.liquidbounce.api.util.WrappedGuiScreen
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.WorkerUtils
import org.lwjgl.input.Keyboard

class GuiModsMenu(private val prevGui: IGuiScreen) : WrappedGuiScreen()
{

	override fun initGui()
	{
		val buttonX = (representedScreen.width shr 1) - 100
		val buttonY = (representedScreen.height shr 2) + 48

		val buttonList = representedScreen.buttonList

		val provider = classProvider

		buttonList.add(provider.createGuiButton(0, buttonX, buttonY, "Forge Mods"))
		buttonList.add(provider.createGuiButton(1, buttonX, buttonY + 25, "Scripts"))
		buttonList.add(provider.createGuiButton(2, buttonX, buttonY + 50, "Rich Presence: ${if (LiquidBounce.clientRichPresence.showRichPresenceValue) "\u00A7aON" else "\u00A7cOFF"}"))
		buttonList.add(provider.createGuiButton(3, buttonX, buttonY + 75, "Back"))
	}

	override fun actionPerformed(button: IGuiButton)
	{
		val provider = classProvider

		when (val id = button.id)
		{
			0 -> mc.displayGuiScreen(provider.createGuiModList(representedScreen))
			1 -> mc.displayGuiScreen(provider.wrapGuiScreen(GuiScripts(representedScreen)))

			2 ->
			{
				val rpc = LiquidBounce.clientRichPresence
				rpc.showRichPresenceValue = when (val state = !rpc.showRichPresenceValue)
				{
					false ->
					{
						rpc.shutdown()
						changeDisplayState(id, state)
						false
					}

					true ->
					{
						var value = true
						WorkerUtils.workers.execute {
							value = try
							{
								rpc.setup()
								true
							}
							catch (throwable: Throwable)
							{
								ClientUtils.logger.error("Failed to setup Discord RPC.", throwable)
								false
							}
						}
						changeDisplayState(id, value)
						value
					}
				}
			}

			3 -> mc.displayGuiScreen(prevGui)
		}
	}

	private fun changeDisplayState(buttonId: Int, state: Boolean)
	{
		val button = representedScreen.buttonList[buttonId]
		val displayName = button.displayString
		button.displayString = when (state)
		{
			false -> displayName.replace("\u00A7aON", "\u00A7cOFF")
			true -> displayName.replace("\u00A7cOFF", "\u00A7aON")
		}
	}

	override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float)
	{
		representedScreen.drawBackground(0)

		Fonts.fontBold180.drawCenteredString("Mods", (representedScreen.width shr 1).toFloat(), (representedScreen.height shr 3) + 5F, 4673984, true)

		super.drawScreen(mouseX, mouseY, partialTicks)
	}

	override fun keyTyped(typedChar: Char, keyCode: Int)
	{
		if (Keyboard.KEY_ESCAPE == keyCode)
		{
			mc.displayGuiScreen(prevGui)
			return
		}

		super.keyTyped(typedChar, keyCode)
	}
}
