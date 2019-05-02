package com.gamerforea.ae.util;

import appeng.api.util.DimensionalCoord;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public final class ChatUtils
{
	public static IChatComponent dimCoordToChatComponent(DimensionalCoord coord, ICommandSender sender)
	{
		if (coord == null)
			return text("Unknown");

		IChatComponent msg = text(String.valueOf(coord));
		if (coord.getWorld() == sender.getEntityWorld())
			setChatClickEvent(msg, ClickEvent.Action.SUGGEST_COMMAND, "/tp " + coord.x + ' ' + coord.y + ' ' + coord.z);
		return msg;
	}

	public static ChatComponentText text(String text)
	{
		return new ChatComponentText(text);
	}

	public static ChatComponentTranslation translation(String format, Object... args)
	{
		return new ChatComponentTranslation(format, args);
	}

	public static <T extends IChatComponent> T color(T chatComponent, EnumChatFormatting color)
	{
		chatComponent.getChatStyle().setColor(color);
		return chatComponent;
	}

	public static <T extends IChatComponent> T setChatClickEvent(T chatComponent, ClickEvent.Action action, String value)
	{
		chatComponent.getChatStyle().setChatClickEvent(new ClickEvent(action, value));
		return chatComponent;
	}
}
