package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving

/**
 * LiquidBounce Hacked Client A minecraft forge injection client using Mixin
 *
 * @author CCBlueX
 * @game   Minecraft
 */
class ACRBHop : SpeedMode("ACR-BHop")
{
	override fun onMotion(eventState: EventState)
	{
		val thePlayer = mc.thePlayer ?: return


		if (isMoving(thePlayer))
		{
			thePlayer.jumpMovementFactor = 0.1F

			thePlayer.motionX /= 1.1
			thePlayer.motionZ /= 1.1

			if (thePlayer.onGround)
			{

				//				val f = direction
				//				thePlayer.motionX -= (functions.sin(f) * 0.2f).toDouble()
				//				thePlayer.motionZ += (functions.cos(f) * 0.2f).toDouble()
				jump(thePlayer)
			}

			MovementUtils.strafe(thePlayer)
		}
		else MovementUtils.zeroXZ(thePlayer)
	}

	override fun onUpdate()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}
}
