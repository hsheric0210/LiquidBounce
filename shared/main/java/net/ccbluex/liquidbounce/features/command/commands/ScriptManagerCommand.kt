/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import org.apache.commons.io.IOUtils
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipFile

class ScriptManagerCommand : Command("scriptmanager", "scripts")
{
	/**
	 * Execute commands with provided [args]
	 */
	override fun execute(args: Array<String>)
	{
		if (args.size > 1)
		{
			when (args[1].toLowerCase())
			{
				"import" ->
				{
					try
					{
						val file = MiscUtils.openFileChooser() ?: return
						val fileName = file.name

						if (fileName.endsWith(".js"))
						{
							LiquidBounce.scriptManager.importScript(file)

							LiquidBounce.clickGui = ClickGui()
							FileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)

							chat("Successfully imported script.")
							return
						} else if (fileName.endsWith(".zip"))
						{
							val zipFile = ZipFile(file)
							val entries = zipFile.entries()
							val scriptFiles = ArrayList<File>()

							while (entries.hasMoreElements())
							{
								val entry = entries.nextElement()
								val entryName = entry.name
								val entryFile = File(LiquidBounce.scriptManager.scriptsFolder, entryName)

								if (entry.isDirectory)
								{
									entryFile.mkdir()
									continue
								}

								val fileStream = zipFile.getInputStream(entry)
								val fileOutputStream = FileOutputStream(entryFile)

								IOUtils.copy(fileStream, fileOutputStream)
								fileOutputStream.close()
								fileStream.close()

								if (!entryName.contains("/")) scriptFiles.add(entryFile)
							}

							scriptFiles.forEach(LiquidBounce.scriptManager::loadScript)

							LiquidBounce.clickGui = ClickGui()
							FileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)
							FileManager.loadConfig(LiquidBounce.fileManager.hudConfig)

							chat("Successfully imported script.")
							return
						}

						chat("The file extension has to be .js or .zip")
					} catch (t: Throwable)
					{
						ClientUtils.logger.error("Something went wrong while importing a script.", t)
						chat("${t.javaClass.name}: ${t.message}")
					}
				}

				"delete" ->
				{
					try
					{
						if (args.size <= 2)
						{
							chatSyntax("scriptmanager delete <index>")
							return
						}

						val scriptIndex = args[2].toInt()
						val scripts = LiquidBounce.scriptManager.scripts

						if (scriptIndex >= scripts.size)
						{
							chat("Index $scriptIndex is too high.")
							return
						}

						val script = scripts[scriptIndex]

						LiquidBounce.scriptManager.deleteScript(script)

						LiquidBounce.clickGui = ClickGui()
						FileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)
						FileManager.loadConfig(LiquidBounce.fileManager.hudConfig)
						chat("Successfully deleted script.")
					} catch (numberFormat: NumberFormatException)
					{
						chatSyntaxError()
					} catch (t: Throwable)
					{
						ClientUtils.logger.error("Something went wrong while deleting a script.", t)
						chat("${t.javaClass.name}: ${t.message}")
					}
				}

				"reload" ->
				{
					try
					{
						LiquidBounce.commandManager = CommandManager()
						LiquidBounce.commandManager.registerCommands()
						LiquidBounce.isStarting = true
						LiquidBounce.scriptManager.reloadScripts()
						for (module in LiquidBounce.moduleManager.modules) LiquidBounce.moduleManager.generateCommand(module)
						FileManager.loadConfig(LiquidBounce.fileManager.modulesConfig)
						LiquidBounce.isStarting = false
						FileManager.loadConfig(LiquidBounce.fileManager.valuesConfig)
						LiquidBounce.clickGui = ClickGui()
						FileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)
						chat("Successfully reloaded all scripts.")
					} catch (t: Throwable)
					{
						ClientUtils.logger.error("Something went wrong while reloading all scripts.", t)
						chat("${t.javaClass.name}: ${t.message}")
					}
				}

				"folder" ->
				{
					try
					{
						Desktop.getDesktop().open(LiquidBounce.scriptManager.scriptsFolder)
						chat("Successfully opened scripts folder.")
					} catch (t: Throwable)
					{
						ClientUtils.logger.error("Something went wrong while trying to open your scripts folder.", t)
						chat("${t.javaClass.name}: ${t.message}")
					}
				}
			}

			return
		}

		val scriptManager = LiquidBounce.scriptManager

		if (scriptManager.scripts.isNotEmpty())
		{
			chat("\u00A7c\u00A7lScripts")
			scriptManager.scripts.forEachIndexed { index, script -> chat("$index: \u00A7a\u00A7l${script.scriptName} \u00A7a\u00A7lv${script.scriptVersion} \u00A73by \u00A7a\u00A7l${script.scriptAuthors.joinToString(", ")}") }
		}

		chatSyntax("scriptmanager <import/delete/reload/folder>")
	}

	override fun tabComplete(args: Array<String>): List<String>
	{
		if (args.isEmpty()) return emptyList()

		return when (args.size)
		{
			1 -> listOf("delete", "import", "folder", "reload").filter { it.startsWith(args[0], true) }
			else -> emptyList()
		}
	}
}
