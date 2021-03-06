/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.ClickWindowEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "InventoryMove", description = "Allows you to walk while an inventory is opened.", category = ModuleCategory.MOVEMENT)
class InventoryMove : Module()
{

	private val undetectable = BoolValue("Undetectable", false)
	val aacAdditionProValue = BoolValue("AACAdditionPro", false) // TODO: Add NoSprint option
	private val noMoveClicksValue = BoolValue("NoMoveClicks", false)

	private val affectedBindings = run {
		val gameSettings = mc.gameSettings
		arrayOf(gameSettings.keyBindForward, gameSettings.keyBindBack, gameSettings.keyBindRight, gameSettings.keyBindLeft, gameSettings.keyBindJump, gameSettings.keyBindSprint)
	}

	@EventTarget
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
	{
		tick()
	}

	private fun tick()
	{
		val provider = classProvider

		val currentScreen = mc.currentScreen
		val gameSettings = mc.gameSettings

		val onlyInventory = undetectable.get()

		if (!provider.isGuiChat(currentScreen) && !provider.isGuiIngameMenu(currentScreen) && (!onlyInventory || !provider.isGuiContainer(currentScreen))) for (affectedBinding in affectedBindings) affectedBinding.pressed = gameSettings.isKeyDown(affectedBinding)
	}

	@EventTarget
	fun onClick(event: ClickWindowEvent)
	{
		if (noMoveClicksValue.get() && MovementUtils.isMoving(mc.thePlayer ?: return)) event.cancelEvent()
	}

	override fun onDisable()
	{
		val isIngame = mc.currentScreen != null

		affectedBindings.filter { !mc.gameSettings.isKeyDown(it) || isIngame }.forEach { it.pressed = false }
	}

	override val tag: String?
		get() = if (aacAdditionProValue.get()) "AACAdditionPro" else null
}
