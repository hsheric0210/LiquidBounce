package net.ccbluex.liquidbounce.features.command.special

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.render.XRay
import net.ccbluex.liquidbounce.file.FileManager
import net.minecraft.block.Block

class XrayCommand : Command("xray")
{

	val xRay = LiquidBounce.moduleManager[XRay::class.java] as XRay

	/**
	 * Execute commands with provided [args]
	 */
	override fun execute(args: Array<String>)
	{
		if (args.size > 1)
		{
			when (args[1].toLowerCase())
			{
				"add" ->
				{
					if (args.size > 2)
					{
						try
						{
							val block = try
							{
								functions.getBlockById(args[2].toInt())
							} catch (exception: NumberFormatException)
							{
								val tmpBlock = functions.getBlockFromName(args[2])

								if (tmpBlock == null || functions.getIdFromBlock(tmpBlock) <= 0)
								{
									chat("\u00A77Block \u00A78${args[2]}\u00A77 does not exist!")
									return
								}

								tmpBlock
							}

							if (block == null || xRay.xrayBlocks.contains(block))
							{
								chat("This block is already on the list.")
								return
							}

							xRay.xrayBlocks.add(block)
							FileManager.saveConfig(LiquidBounce.fileManager.xrayConfig)
							chat("\u00A77Added block \u00A78${block.localizedName}\u00A77.")
							playEdit()
						} catch (exception: NumberFormatException)
						{
							chatSyntaxError()
						}

						return
					}

					chatSyntax("xray add <block_id>")
					return
				}

				"remove" ->
				{
					if (args.size > 2)
					{
						try
						{
							val block = try
							{
								functions.getBlockById(args[2].toInt())
							} catch (exception: NumberFormatException)
							{
								val tmpBlock = functions.getBlockFromName(args[2])

								if (tmpBlock == null || functions.getIdFromBlock(tmpBlock) <= 0)
								{
									chat("\u00A77Block \u00A78${args[2]}\u00A77 does not exist!")
									return
								}

								tmpBlock
							}

							if (block == null || !xRay.xrayBlocks.contains(block))
							{
								chat("This block is not on the list.")
								return
							}

							xRay.xrayBlocks.remove(block)
							FileManager.saveConfig(LiquidBounce.fileManager.xrayConfig)
							chat("\u00A77Removed block \u00A78${block.localizedName}\u00A77.")
							playEdit()
						} catch (exception: NumberFormatException)
						{
							chatSyntaxError()
						}

						return
					}
					chatSyntax("xray remove <block_id>")
					return
				}

				"list" ->
				{
					chat("\u00A78Xray blocks:")
					xRay.xrayBlocks.forEach { chat("\u00A78${it.localizedName} \u00A77-\u00A7c ${functions.getIdFromBlock(it)}") }
					return
				}
			}
		}
		chatSyntax("xray <add, remove, list>")
	}

	override fun tabComplete(args: Array<String>): List<String>
	{
		if (args.isEmpty()) return emptyList()

		return when (args.size)
		{
			1 -> arrayOf("add", "remove", "list").filter { it.startsWith(args[0], ignoreCase = true) }

			2 -> when (args[0].toLowerCase())
			{
				"add" -> return functions.getBlockRegistryKeys().asSequence().map { it.resourcePath.toLowerCase() }.filter { Block.getBlockFromName(it.toLowerCase()) != null }
					.filter { !xRay.xrayBlocks.contains(functions.getBlockFromName(it.toLowerCase())) }.filter { it.startsWith(args[1], true) }.toList()
				"remove" -> return functions.getBlockRegistryKeys().asSequence().map { it.resourcePath.toLowerCase() }.filter { xRay.xrayBlocks.contains(functions.getBlockFromName(it)) }.filter { it.startsWith(args[1], true) }.toList()
				else -> emptyList()
			}

			else -> emptyList()
		}
	}
}
