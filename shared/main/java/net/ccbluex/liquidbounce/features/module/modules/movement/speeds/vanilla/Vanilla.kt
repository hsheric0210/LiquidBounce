package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vanilla

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

/**
 * LiquidBounce Hacked Client A minecraft forge injection client using Mixin
 *
 * @author CCBlueX
 * @game   Minecraft
 */
class Vanilla : SpeedMode("Vanilla")
{
	override fun onMotion(eventState: EventState)
	{
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
		val thePlayer = mc.thePlayer ?: return

		if (MovementUtils.isMoving(thePlayer))
		{
			val func = functions

			val moveSpeed = (LiquidBounce.moduleManager[Speed::class.java] as Speed).vanillaSpeedValue.get()
			val dir = MovementUtils.getDirection(thePlayer)
			event.x = (-func.sin(dir) * moveSpeed).toDouble()
			event.z = (func.cos(dir) * moveSpeed).toDouble()
		}
	}
}
