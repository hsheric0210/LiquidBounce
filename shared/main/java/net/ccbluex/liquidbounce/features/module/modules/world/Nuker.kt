/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.enums.EnumFacingType
import net.ccbluex.liquidbounce.api.minecraft.client.block.IBlock
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketPlayerDigging
import net.ccbluex.liquidbounce.api.minecraft.util.WBlockPos
import net.ccbluex.liquidbounce.api.minecraft.util.WVec3
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.player.AutoTool
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "Nuker", description = "Breaks all blocks around you.", category = ModuleCategory.WORLD)
class Nuker : Module()
{

	/**
	 * OPTIONS
	 */

	private val radiusValue = FloatValue("Radius", 5.2F, 1F, 6F)
	private val throughWallsValue = BoolValue("ThroughWalls", false)
	private val priorityValue = ListValue("Priority", arrayOf("Distance", "Hardness"), "Distance")
	private val rotationsValue = BoolValue("Rotations", true)
	private val layerValue = BoolValue("Layer", false)
	private val hitDelayValue = IntegerValue("HitDelay", 4, 0, 20)
	private val nukeValue = IntegerValue("Nuke", 1, 1, 20)
	private val nukeDelay = IntegerValue("NukeDelay", 1, 1, 20)

	/**
	 * VALUES
	 */

	private val attackedBlocks = arrayListOf<WBlockPos>()
	var currentBlock: WBlockPos? = null
	private var blockHitDelay = 0

	private var nukeTimer = TickTimer()
	private var nuke = 0

	@EventTarget
	fun onUpdate(@Suppress("UNUSED_PARAMETER") event: UpdateEvent)
	{
		val moduleManager = LiquidBounce.moduleManager

		// Block hit delay
		if (blockHitDelay > 0 && !moduleManager[FastBreak::class.java].state)
		{
			blockHitDelay--
			return
		}

		// Reset bps
		nukeTimer.update()
		if (nukeTimer.hasTimePassed(nukeDelay.get()))
		{
			nuke = 0
			nukeTimer.reset()
		}

		// Clear blocks
		attackedBlocks.clear()

		val theWorld = mc.theWorld ?: return
		val thePlayer = mc.thePlayer ?: return
		val posX = thePlayer.posX
		val posZ = thePlayer.posZ

		val playerController = mc.playerController
		val netHandler = mc.netHandler

		val radius = radiusValue.get()
		val throughWalls = throughWallsValue.get()
		val nukeEnabled = nukeValue.get()
		val priority = priorityValue.get()
		val layer = layerValue.get()

		val provider = classProvider

		if (!playerController.isInCreativeMode)
		{

			// Default nuker

			val validBlocks = searchBlocks(theWorld, thePlayer, radius.roundToInt() + 1).filterValues(::validBlock).filterKeys { getCenterDistance(thePlayer, it) <= radius }.run { if (layer) filterKeys { it.y >= thePlayer.posY } else this }.run {
				if (throughWalls) this
				else filterKeys { pos ->
					// ThroughWalls: Just break blocks in your sight
					// Raytrace player eyes to block position (through walls check)
					val eyesPos = WVec3(posX, thePlayer.entityBoundingBox.minY + thePlayer.eyeHeight, posZ)
					val blockVec = WVec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
					val rayTrace = theWorld.rayTraceBlocks(eyesPos, blockVec, stopOnLiquid = false, ignoreBlockWithoutBoundingBox = true, returnLastUncollidableBlock = false)

					// Check if block is visible
					rayTrace != null && rayTrace.blockPos == pos
				}
			}.toMutableMap()

			do
			{
				val (blockPos, block) = when (priority)
				{
					"Distance" -> validBlocks.minBy { (pos, _) ->
						val distance = getCenterDistance(thePlayer, pos)
						val safePos = WBlockPos(posX, thePlayer.posY - 1, posZ)

						if (pos.x == safePos.x && safePos.y <= pos.y && pos.z == safePos.z) Double.MAX_VALUE - distance // Last block
						else distance
					}

					"Hardness" -> validBlocks.maxBy { (pos, block) ->
						val hardness = block.getPlayerRelativeBlockHardness(thePlayer, theWorld, pos).toDouble()
						val safePos = WBlockPos(posX, thePlayer.posY - 1, posZ)

						if (pos.x == safePos.x && safePos.y <= pos.y && pos.z == safePos.z) Double.MIN_VALUE + hardness // Last block
						else hardness
					}

					else -> return // what? why?
				} ?: return // well I guess there is no block to break :(

				// Reset current damage in case of block switch
				if (blockPos != currentBlock) currentDamage = 0F

				// Change head rotations to next block
				if (rotationsValue.get())
				{
					val rotation = RotationUtils.faceBlock(theWorld, thePlayer, blockPos) ?: return // In case of a mistake. Prevent flag.
					RotationUtils.setTargetRotation(rotation.rotation)
				}

				// Set next target block
				currentBlock = blockPos
				attackedBlocks.add(blockPos)

				// Call auto tool
				val autoTool = moduleManager[AutoTool::class.java] as AutoTool
				if (autoTool.state) autoTool.switchSlot(blockPos)

				val hitDelay = hitDelayValue.get()

				// Start block breaking
				if (currentDamage == 0F)
				{
					netHandler.addToSendQueue(provider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.START_DESTROY_BLOCK, blockPos, provider.getEnumFacing(EnumFacingType.DOWN)))

					// End block break if able to break instant
					if (block.getPlayerRelativeBlockHardness(thePlayer, theWorld, blockPos) >= 1F)
					{
						currentDamage = 0F

						thePlayer.swingItem()
						playerController.onPlayerDestroyBlock(blockPos, provider.getEnumFacing(EnumFacingType.DOWN))

						blockHitDelay = hitDelay
						validBlocks -= blockPos
						nuke++

						continue // Next break
					}
				}

				// Break block
				thePlayer.swingItem()
				currentDamage += block.getPlayerRelativeBlockHardness(thePlayer, theWorld, blockPos)
				theWorld.sendBlockBreakProgress(thePlayer.entityId, blockPos, (currentDamage * 10F).toInt() - 1)

				// End of breaking block
				if (currentDamage >= 1F)
				{
					netHandler.addToSendQueue(provider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.STOP_DESTROY_BLOCK, blockPos, provider.getEnumFacing(EnumFacingType.DOWN)))
					playerController.onPlayerDestroyBlock(blockPos, provider.getEnumFacing(EnumFacingType.DOWN))

					blockHitDelay = hitDelay
					currentDamage = 0F
				}
				return // Break out
			} while (nuke < nukeEnabled)
		}
		else
		{

			// Fast creative mode nuker (CreativeStorm option)

			// Unable to break with swords in creative mode
			if (provider.isItemSword(thePlayer.heldItem?.item)) return

			// Search for new blocks to break
			searchBlocks(theWorld, thePlayer, radius.roundToInt() + 1).filterValues(::validBlock).filterKeys { getCenterDistance(thePlayer, it) <= radius }.filterKeys { !layer || it.y >= thePlayer.posY }.run {
				if (throughWalls) this
				else filterKeys { pos ->
					// ThroughWalls: Just break blocks in your sight
					// Raytrace player eyes to block position (through walls check)

					// Check if block is visible

					(theWorld.rayTraceBlocks(WVec3(posX, thePlayer.entityBoundingBox.minY + thePlayer.eyeHeight, posZ), WVec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5), stopOnLiquid = false, ignoreBlockWithoutBoundingBox = true, returnLastUncollidableBlock = false) ?: return@filterKeys false).blockPos == pos
				}
			}.forEach { (pos, _) -> // Instant break block
				netHandler.addToSendQueue(provider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.START_DESTROY_BLOCK, pos, provider.getEnumFacing(EnumFacingType.DOWN)))
				thePlayer.swingItem()
				netHandler.addToSendQueue(provider.createCPacketPlayerDigging(ICPacketPlayerDigging.WAction.STOP_DESTROY_BLOCK, pos, provider.getEnumFacing(EnumFacingType.DOWN)))
				attackedBlocks.add(pos)
			}
		}
	}

	@EventTarget
	fun onRender3D(@Suppress("UNUSED_PARAMETER") event: Render3DEvent)
	{
		val theWorld = mc.theWorld ?: return
		val thePlayer = mc.thePlayer ?: return

		// Safe block
		if (!layerValue.get())
		{
			val safePos = WBlockPos(thePlayer.posX, thePlayer.posY - 1, thePlayer.posZ)
			val safeBlock = BlockUtils.getBlock(theWorld, safePos)
			if (validBlock(safeBlock)) RenderUtils.drawBlockBox(theWorld, thePlayer, safePos, Color.GREEN, outline = true, hydraESP = false)
		}

		// Just draw all blocks
		for (blockPos in attackedBlocks) RenderUtils.drawBlockBox(theWorld, thePlayer, blockPos, Color.RED, outline = true, hydraESP = false)
	}

	/**
	 * Check if [block] is a valid block to break
	 */
	private fun validBlock(block: IBlock): Boolean
	{
		val provider = classProvider

		return !provider.isBlockAir(block) && !provider.isBlockLiquid(block) && !provider.isBlockBedrock(block)
	}

	companion object
	{
		var currentDamage = 0F
	}

	override val tag: String
		get() = "${radiusValue.get()}"
}
