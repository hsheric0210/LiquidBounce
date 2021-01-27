/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.api.minecraft.client.multiplayer.IServerData;
import net.ccbluex.liquidbounce.ui.client.GuiMainMenu;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public final class ServerUtils extends MinecraftInstance
{
	public static IServerData lastServerData;

	public static void connectToLastServer()
	{
		if (lastServerData == null)
			return;

		mc.displayGuiScreen(classProvider.createGuiConnecting(classProvider.createGuiMultiplayer(classProvider.wrapGuiScreen(new GuiMainMenu())), mc, lastServerData));
	}

	public static String getRemoteIp()
	{
		if (mc.getTheWorld() == null)
			return "World is null";

		if (mc.getTheWorld().isRemote())
		{
			final IServerData serverData = mc.getCurrentServerData();

			if (serverData != null)
				return serverData.getServerIP();
		}

		return "Singleplayer";
	}

	@Nullable
	public static String getLastServerIp()
	{
		return lastServerData != null && !mc.isIntegratedServerRunning() ? lastServerData.getServerIP() : null;
	}
}
