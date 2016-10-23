package com.gamerforea.ae;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

import java.util.Set;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static final Set<String> pilonBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> formationPlaneBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> annihilationPlaneBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> autoCraftBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static float chargedStaffDamage = 6F;

	static
	{
		try
		{
			final Configuration cfg = FastUtils.getConfig("AppEng");
			readStringSet(cfg, "pilonBlackList", CATEGORY_GENERAL, "Чёрный список блоков для Пилонов", pilonBlackList);
			readStringSet(cfg, "formationPlaneBlackList", CATEGORY_GENERAL, "Чёрный список блоков для Плоскости формирования", formationPlaneBlackList);
			readStringSet(cfg, "annihilationPlaneBlackList", CATEGORY_GENERAL, "Чёрный список блоков для Плоскости истребления", annihilationPlaneBlackList);
			readStringSet(cfg, "autoCraftBlackList", CATEGORY_GENERAL, "Чёрный список блоков для автокрафта", autoCraftBlackList);
			chargedStaffDamage = cfg.getFloat("chargedStaffDamage", CATEGORY_GENERAL, chargedStaffDamage, 0, Float.MAX_VALUE, "Урон Заряженного посоха");
			cfg.save();
		}
		catch (final Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static final boolean inBlackList(Set<String> blackList, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inBlackList(blackList, ((ItemBlock) item).field_150939_a, meta);

		return inBlackList(blackList, GameRegistry.findUniqueIdentifierFor(item), meta);
	}

	public static final boolean inBlackList(Set<String> blackList, Block block, int meta)
	{
		return inBlackList(blackList, GameRegistry.findUniqueIdentifierFor(block), meta);
	}

	private static final boolean inBlackList(Set<String> blackList, UniqueIdentifier id, int meta)
	{
		if (id != null)
		{
			String name = id.modId + ':' + id.name;
			if (blackList.contains(name) || blackList.contains(name + ':' + meta))
				return true;
		}

		return false;
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