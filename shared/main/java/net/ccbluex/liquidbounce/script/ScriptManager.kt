/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.script

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.ClientUtils
import java.io.File
import java.io.FileFilter

class ScriptManager
{
	val scripts = mutableListOf<Script>()

	val scriptsFolder = File(LiquidBounce.fileManager.dir, "scripts")
	private val scriptFileExtension = ".js"

	/**
	 * Loads all scripts inside the scripts folder.
	 */
	fun loadScripts()
	{
		if (!scriptsFolder.exists()) scriptsFolder.mkdir()

		scriptsFolder.listFiles(FileFilter { it.name.endsWith(scriptFileExtension) })?.forEach(::loadScript)
	}

	/**
	 * Unloads all scripts.
	 */
	fun unloadScripts()
	{
		scripts.clear()
	}

	/**
	 * Loads a script from a file.
	 */
	fun loadScript(scriptFile: File)
	{
		try
		{
			val script = Script(scriptFile)
			script.initScript()
			scripts.add(script)
		} catch (t: Throwable)
		{
			ClientUtils.logger.error("[ScriptAPI] Failed to load script '${scriptFile.name}'.", t)
		}
	}

	/**
	 * Enables all scripts.
	 */
	fun enableScripts()
	{
		scripts.forEach(Script::onEnable)
	}

	/**
	 * Disables all scripts.
	 */
	fun disableScripts()
	{
		scripts.forEach(Script::onDisable)
	}

	/**
	 * Imports a script.
	 * @param file JavaScript file to be imported.
	 */
	fun importScript(file: File)
	{
		val scriptFile = File(scriptsFolder, file.name)
		file.copyTo(scriptFile)

		loadScript(scriptFile)
		ClientUtils.logger.info("[ScriptAPI] Successfully imported script '${scriptFile.name}'.")
	}

	/**
	 * Deletes a script.
	 * @param script Script to be deleted.
	 */
	fun deleteScript(script: Script)
	{
		script.onDisable()
		scripts.remove(script)
		script.scriptFile.delete()

		ClientUtils.logger.info("[ScriptAPI]  Successfully deleted script '${script.scriptFile.name}'.")
	}

	/**
	 * Reloads all scripts.
	 */
	fun reloadScripts()
	{
		disableScripts()
		unloadScripts()
		loadScripts()
		enableScripts()

		ClientUtils.logger.info("[ScriptAPI]  Successfully reloaded scripts.")
	}
}
