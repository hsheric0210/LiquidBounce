/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "ReverseStep", description = "Allows you to step down blocks faster.", category = ModuleCategory.MOVEMENT)
class ReverseStep : Module()
{
	private val motionValue = FloatValue("Motion", 1f, 0.21f, 1f)
	private var jumped = false

	@EventTarget(ignoreCondition = true)
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent?)
	{
		val theWorld = mc.theWorld ?: return
		val thePlayer = mc.thePlayer ?: return

		if (thePlayer.onGround) jumped = false

		if (thePlayer.motionY > 0) jumped = true

		if (!state) return

		val entityBoundingBox = thePlayer.entityBoundingBox

		if (collideBlock(theWorld, thePlayer, entityBoundingBox, classProvider::isBlockLiquid) || collideBlock(theWorld, thePlayer, classProvider.createAxisAlignedBB(entityBoundingBox.maxX, entityBoundingBox.maxY, entityBoundingBox.maxZ, entityBoundingBox.minX, entityBoundingBox.minY - 0.01, entityBoundingBox.minZ), classProvider::isBlockLiquid)) return

		if (!mc.gameSettings.keyBindJump.isKeyDown && !thePlayer.onGround && !thePlayer.movementInput.jump && thePlayer.motionY <= 0.0 && thePlayer.fallDistance <= 1f && !jumped) thePlayer.motionY = (-motionValue.get()).toDouble()
	}

	@EventTarget(ignoreCondition = true)
	fun onJump(@Suppress("UNUSED_PARAMETER") event: JumpEvent?)
	{
		jumped = true
	}

	override val tag: String
		get() = "${motionValue.get()}"
}
