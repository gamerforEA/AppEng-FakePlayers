package com.gamerforea.ae;

import java.util.Set;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;

import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static Set<String> pilonBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");

	static
	{
		try
		{
			Configuration config = FastUtils.getConfig("AppEng");
			pilonBlackList = Sets.newHashSet(config.getStringList("pilonBlackList", Configuration.CATEGORY_GENERAL, pilonBlackList.toArray(new String[pilonBlackList.size()]), "Чёрный список блоков для Пилонов"));
			config.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}