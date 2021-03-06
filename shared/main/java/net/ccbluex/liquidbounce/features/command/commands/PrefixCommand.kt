/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.file.FileManager

class PrefixCommand : Command("prefix")
{
	/**
	 * Execute commands with provided [args]
	 */
	override fun execute(args: Array<String>)
	{
		val thePlayer = mc.thePlayer

		if (args.size <= 1)
		{
			chatSyntax(thePlayer, "prefix <character>")
			return
		}

		val prefix = args[1]

		if (prefix.length > 1)
		{
			chat(thePlayer, "\u00A7cPrefix can only be one character long!")
			return
		}

		LiquidBounce.commandManager.prefix = prefix.single()
		FileManager.saveConfig(LiquidBounce.fileManager.valuesConfig)

		chat(thePlayer, "Successfully changed command prefix to '\u00A78$prefix\u00A73'")
	}
}
