/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.api.enums.ItemType
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.misc.StringUtils

class HoloStandCommand : Command("holostand")
{
	/**
	 * Execute commands with provided [args]
	 */
	override fun execute(args: Array<String>)
	{
		val thePlayer = mc.thePlayer

		if (args.size > 4)
		{
			if (mc.playerController.isNotCreative)
			{
				chat(thePlayer, "\u00A7c\u00A7lError: \u00A73You need to be in creative mode.")
				return
			}

			try
			{
				val x = args[1].toDouble()
				val y = args[2].toDouble()
				val z = args[3].toDouble()
				val message = StringUtils.toCompleteString(args, 4)

				val provider = classProvider

				val itemStack = provider.createItemStack(provider.getItemEnum(ItemType.ARMOR_STAND))
				val base = provider.createNBTTagCompound()
				val entityTag = provider.createNBTTagCompound()

				entityTag.setInteger("Invisible", 1)
				entityTag.setString("CustomName", message)
				entityTag.setInteger("CustomNameVisible", 1)
				entityTag.setInteger("NoGravity", 1)

				val position = provider.createNBTTagList()
				position.appendTag(provider.createNBTTagDouble(x))
				position.appendTag(provider.createNBTTagDouble(y))
				position.appendTag(provider.createNBTTagDouble(z))
				entityTag.setTag("Pos", position)
				base.setTag("EntityTag", entityTag)
				itemStack.tagCompound = base
				itemStack.setStackDisplayName("\u00A7c\u00A7lHolo\u00A7eStand")
				mc.netHandler.addToSendQueue(provider.createCPacketCreativeInventoryAction(36, itemStack))

				chat(thePlayer, "The HoloStand was successfully added to your inventory.")
			}
			catch (exception: NumberFormatException)
			{
				chatSyntaxError(thePlayer)
			}

			return
		}

		chatSyntax(thePlayer, "holostand <x> <y> <z> <message...>")
	}
}
