/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixBHop : SpeedMode("Matrix-BHop")
{

	override fun onUpdate()
	{
		val thePlayer = mc.thePlayer ?: return

		if (thePlayer.isInWater) return
		if (MovementUtils.isMoving)
		{
			if (thePlayer.onGround)
			{
				thePlayer.jump()
				thePlayer.speedInAir = 0.02098f
				mc.timer.timerSpeed = 1.055f
			} else MovementUtils.strafe(MovementUtils.speed)
		} else mc.timer.timerSpeed = 1f
	}

	override fun onMotion()
	{
	}

	override fun onMove(event: MoveEvent)
	{
	}

	override fun onDisable()
	{
		(mc.thePlayer ?: return).speedInAir = 0.02f
		mc.timer.timerSpeed = 1f
	}
}