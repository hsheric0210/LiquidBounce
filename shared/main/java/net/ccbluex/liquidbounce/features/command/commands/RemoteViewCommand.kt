/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.api.minecraft.client.entity.player.IEntityPlayer
import net.ccbluex.liquidbounce.features.command.Command

class RemoteViewCommand : Command("remoteview", "rv")
{
	/**
	 * Execute commands with provided [args]
	 */
	override fun execute(args: Array<String>)
	{
		val thePlayer = mc.thePlayer

		if (args.size < 2)
		{
			if (mc.renderViewEntity != thePlayer)
			{
				mc.renderViewEntity = thePlayer
				return
			}

			chatSyntax(thePlayer, "remoteview <username>")

			return
		}

		val targetName = args[1]

		(mc.theWorld ?: return).loadedEntityList.firstOrNull { targetName == it.name }?.let {
			mc.renderViewEntity = it
			chat(thePlayer, "Now viewing perspective of \u00A78${it.name}\u00A73.")
			chat(thePlayer, "Execute \u00A78${LiquidBounce.commandManager.prefix}remoteview \u00A73again to go back to yours.")
		}
	}

	override fun tabComplete(args: Array<String>): List<String>
	{
		if (args.isEmpty()) return emptyList()

		val theWorld = mc.theWorld ?: return emptyList()

		return when (args.size)
		{
			1 -> return theWorld.playerEntities.map(IEntityPlayer::name).filter { it.startsWith(args[0], true) }.toList()
			else -> emptyList()
		}
	}
}
