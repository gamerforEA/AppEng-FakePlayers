package com.gamerforea.ae;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

import java.util.Set;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;

import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static final Set<String> pilonBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static float chargedStaffDamage = 6F;

	static
	{
		try
		{
			final Configuration cfg = FastUtils.getConfig("AppEng");
			readStringSet(cfg, "pilonBlackList", CATEGORY_GENERAL, "Чёрный список блоков для Пилонов", pilonBlackList);
			chargedStaffDamage = cfg.getFloat("chargedStaffDamage", CATEGORY_GENERAL, chargedStaffDamage, 0, Float.MAX_VALUE, "Урон Заряженного посоха");
			cfg.save();
		}
		catch (final Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	private static final void readStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		final Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static final Set<String> getStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[def.size()]));
	}

	private static final Set<String> getStringSet(final Configuration cfg, final String name, final String category, final String comment, final String... def)
	{
		return Sets.newHashSet(cfg.getStringList(name, category, def, comment));
	}
}