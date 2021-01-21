/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.special;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.ccbluex.liquidbounce.api.minecraft.network.IPacket;
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketCustomPayload;
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ISPacketChat;
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ISPacketCustomPayload;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;

import io.netty.buffer.Unpooled;

/**
 * @author UnstoppableLeaks @ blackspigot.com
 */
public class AntiModDisable extends MinecraftInstance implements Listenable
{

	private static final Pattern CHATCOLOR_RESET_PATTERN = Pattern.compile("\u00a7r", Pattern.LITERAL);

	public static boolean enabled = true;
	public static boolean blockFMLPackets = true;
	public static boolean blockFMLProxyPackets = true;
	public static boolean blockClientBrandRetrieverPackets = true;
	public static boolean blockWDLPayloads = true;
	public static boolean blockBetterSprintingPayloads = true;
	public static boolean block5zigsmodPayloads = true;
	public static boolean blockPermissionsReplPayloads = true;
	public static boolean blockDIPermissionsPayloads = true;
	public static boolean blockCrackedVapeSabotages = true;
	public static boolean blockSchematicaPayloads = true;

	public static boolean debug = false;

	@EventTarget
	public void onPacket(final PacketEvent event)
	{
		final IPacket packet = event.getPacket();

		if (enabled && !mc.isIntegratedServerRunning())
			try
			{
				if (blockFMLProxyPackets && packet.getClass().getName().equals("net.minecraftforge.fml.common.network.internal.FMLProxyPacket"))
					action(event, "FMLProxyPacket", "BLOCKED");

				if (classProvider.isCPacketCustomPayload(packet))
				{
					final ICPacketCustomPayload customPayload = packet.asCPacketCustomPayload();
					final String channelName = customPayload.getChannelName();
					final String upperChannelName = channelName.toUpperCase(Locale.ROOT);

					// Block ClientBrandRetriever packets
					if (blockClientBrandRetrieverPackets && "MC|Brand".equalsIgnoreCase(customPayload.getChannelName()))
					{
						customPayload.setData(classProvider.createPacketBuffer(Unpooled.buffer()).writeString("vanilla"));
						action(event, "ClientBrandRetriever", "SPOOFED");
					}
					if (blockFMLPackets)
					{
						String fmlChannelName = null;

						if (Stream.of("FML", "FORGE", "REGISTER").anyMatch(channelName::equalsIgnoreCase))
							fmlChannelName = channelName;

						if (fmlChannelName != null)
							action(event, String.format("FML packet (%s)", fmlChannelName), "BLOCKED");
					}

					if (blockWDLPayloads && upperChannelName.contains("WDL"))
						action(event, String.format("World Downloader (%s)", channelName), "BLOCKED");

					if (blockBetterSprintingPayloads && "BSprint".equalsIgnoreCase(channelName))
						action(event, String.format("Better Sprinting mod (%s)", channelName), "BLOCKED");

					if (block5zigsmodPayloads && upperChannelName.startsWith("5ZIG"))
						action(event, String.format("The 5zig's mod (%s)", channelName), "BLOCKED");

					if (blockPermissionsReplPayloads && "PERMISSIONSREPL".equalsIgnoreCase(channelName))
						action(event, String.format("(?) (%s)", channelName), "BLOCKED");
				}
				else if (classProvider.isSPacketChat(packet))
				{
					final ISPacketChat chat = packet.asSPacketChat();
					final String text = chat.getChatComponent().getUnformattedText();

					if (blockCrackedVapeSabotages)
					{
						// Cracked vapes are responding 'I'm using cracked vape!' when a specified chat is received.
						if (text.contains("\u00a7c \u00a7r\u00a75 \u00a7r\u00a71 \u00a7r\u00a7f"))
							action(event, "CrackedVape Type A(type: \"&C &R &5 &R &1 &R &F\", text: \"" + text + "\")", "BLOCKED");

						if (CHATCOLOR_RESET_PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement("")).contains("\u00a73 \u00a76 \u00a73 \u00a76 \u00a73 \u00a76 \u00a7d"))
							action(event, "CrackedVape Type B(type: \"&3 &6 &3 &6 &3 &6 &D\", text: \"" + text + "\")", "BLOCKED");

						if (text.contains("\u00a70\u00a71") && text.contains("\u00a7f\u00a7f"))
							action(event, "CrackedVape Type C(type: \"&0&1\" and \"&F&F\"; text: \"" + text + "\")", "BLOCKED");

						if (text.contains("\u00a70\u00a72\u00a70\u00a70\u00a7e\u00a7f"))
							action(event, "CrackedVape Type D(text: \"&0&2&0&0&E&F\", text: \"" + text + "\")", "BLOCKED");

						if (text.contains("\u00a70\u00a72\u00a71\u00a70\u00a7e\u00a7f"))
							action(event, "CrackedVape Type E(text: \"&0&2&1&0&E&F\", text: \"" + text + "\")", "BLOCKED");

						if (text.contains("\u00a70\u00a72\u00a71\u00a71\u00a7e\u00a7f"))
							action(event, "CrackedVape Type F(text: \"&0&2&1&1&E&F\", text: \"" + text + "\")", "BLOCKED");

						if (text.startsWith("\u00a70\u00a70") && text.endsWith("\u00a7e\u00a7f"))
							action(event, "CrackedVape Type G(type: startsWith \"&0&0\" and endsWith \"&E&F\", text: \"" + text + "\")", "BLOCKED");
					}
				}
				else if (classProvider.isSPacketCustomPayload(packet))
				{
					final ISPacketCustomPayload customPayload = packet.asSPacketCustomPayload();
					final String channelName = customPayload.getChannelName();
					final String upperChannelName = channelName.toUpperCase(Locale.ROOT);

					if (blockWDLPayloads && upperChannelName.contains("WDL"))
						action(event, String.format("World Downloader (%s)", channelName), "BLOCKED");

					if (blockBetterSprintingPayloads && "BSM".equalsIgnoreCase(channelName))
						action(event, String.format("Better Sprinting mod (%s)", channelName), "BLOCKED");

					if (block5zigsmodPayloads && upperChannelName.startsWith("5ZIG"))
						action(event, String.format("The 5zig's mod (%s)", channelName), "BLOCKED");

					if (blockPermissionsReplPayloads && "PERMISSIONSREPL".equalsIgnoreCase(channelName))
						action(event, String.format("(?) (%s)", channelName), "BLOCKED");

					if (blockDIPermissionsPayloads && "DIPermissions".equalsIgnoreCase(channelName))
						action(event, String.format("(?) (%s)", channelName), "BLOCKED");

					if (blockCrackedVapeSabotages && upperChannelName.contains("LOLIMAHCKER"))
						action(event, String.format("CrackedVape Type H(type: Custom payload channel \"%s\")", channelName), "BLOCKED");

					if (blockSchematicaPayloads && "schematica".equalsIgnoreCase(channelName))
						action(event, String.format("(?) (%s)", channelName), "BLOCKED");
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
	}

	public static boolean onForgeChannelPacket(final String channelName)
	{
		if (enabled && !mc.isIntegratedServerRunning())
		{
			final String upperChannelName = channelName.toUpperCase(Locale.ROOT);
			if (blockWDLPayloads && upperChannelName.contains("WDL"))
			{
				action(null, String.format("ForgeChannelPacket - World Downloader (%s)", channelName), "BLOCKED");
				return true;
			}

			if (blockBetterSprintingPayloads && "BSM".equalsIgnoreCase(channelName))
			{
				action(null, String.format("ForgeChannelPacket - Better Sprinting mod (%s)", channelName), "BLOCKED");
				return true;
			}

			if (block5zigsmodPayloads && channelName.toUpperCase().contains("5ZIG"))
			{
				action(null, String.format("ForgeChannelPacket - The 5zig's mod (%s)", channelName), "BLOCKED");
				return true;
			}

			if (blockPermissionsReplPayloads && "PERMISSIONSREPL".equalsIgnoreCase(channelName))
			{
				action(null, String.format("ForgeChannelPacket - (?) (%s)", channelName), "BLOCKED");
				return true;
			}

			if (blockDIPermissionsPayloads && "DIPERMISSIONS".equalsIgnoreCase(channelName))
			{
				action(null, String.format("ForgeChannelPacket - (?) (%s\")", channelName), "BLOCKED");
				return true;
			}

			if (blockCrackedVapeSabotages && upperChannelName.contains("LOLIMAHCKER"))
			{
				action(null, String.format("CrackedVape Type I(type: ForgeChannelPacket channel \"%s\")", channelName), "BLOCKED");
				return true;
			}

			if (blockSchematicaPayloads && "schematica".equalsIgnoreCase(channelName))
			{
				action(null, String.format("ForgeChannelPacket - (?) (%s)", channelName), "BLOCKED");
				return true;
			}
		}
		return false;
	}

	private static void action(final PacketEvent event, final String type, final String action)
	{
		if (action.equalsIgnoreCase("BLOCKED") && event != null)
			event.cancelEvent();

		if (debug)
			ClientUtils.displayChatMessage("[AntiModDisable] " + type + " -> " + action);
	}

	@Override
	public boolean handleEvents()
	{
		return true;
	}
}