package com.gamerforea.ae;

import java.util.Set;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static final Set<String> pilonBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> formationPlaneBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> annihilationPlaneBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> autoCraftBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> busBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static String securityBypassPermission = "appeng.security.bypass";
	public static float chargedStaffDamage = 6F;
	public static int autoCraftFixMode = 0;
	public static boolean clearInvOnBreak = true;
	public static boolean annihilationPlaneNoBreakInv = true;
	public static boolean guiOnePlayer = false;

	public static boolean busSameChunk = false;
	public static boolean busSameChunkStorageOnly = false;
	public static boolean busSameChunkMessage = false;

	static
	{
		try
		{
			final Configuration cfg = FastUtils.getConfig("AppEng");
			String c = Configuration.CATEGORY_GENERAL;

			readStringSet(cfg, "pilonBlackList", c, "Чёрный список блоков для Пилонов", pilonBlackList);
			readStringSet(cfg, "formationPlaneBlackList", c, "Чёрный список блоков для Плоскости формирования", formationPlaneBlackList);
			readStringSet(cfg, "annihilationPlaneBlackList", c, "Чёрный список блоков для Плоскости истребления", annihilationPlaneBlackList);
			readStringSet(cfg, "autoCraftBlackList", c, "Чёрный список блоков для автокрафта", autoCraftBlackList);
			readStringSet(cfg, "busBlackList", c, "Чёрный список блоков для шин импорта/экспорта и интерфейсов", busBlackList);
			securityBypassPermission = cfg.getString("securityBypassPermission", c, securityBypassPermission, "Permission для игнорирования защиты AE2-сети");
			chargedStaffDamage = cfg.getFloat("chargedStaffDamage", c, chargedStaffDamage, 0, Float.MAX_VALUE, "Урон Заряженного посоха");
			autoCraftFixMode = cfg.getInt("autoCraftFixMode", c, autoCraftFixMode, 0, 2, "Режим фикса дюпа с автокрафтом [0 - старый фикс; 1 - ненадёжный фикс; 2 - экспериментальный фикс]");
			clearInvOnBreak = cfg.getBoolean("clearInvOnBreak", c, clearInvOnBreak, "Очистка инвентаря блока при его разрушении (защита от дюпа)");
			annihilationPlaneNoBreakInv = cfg.getBoolean("annihilationPlaneNoBreakInv", c, annihilationPlaneNoBreakInv, "Плоскость истребления не может ломать блоки с инвентарями");
			guiOnePlayer = cfg.getBoolean("guiOnePlayer", c, guiOnePlayer, "GUI может открыть только один игрок одновременно");

			busSameChunk = cfg.getBoolean("busSameChunk", c, busSameChunk, "Шины работают с блоками, только если находятся в одном чанке");
			busSameChunkStorageOnly = cfg.getBoolean("busSameChunkStorageOnly", c, busSameChunkStorageOnly, "Проверять чанки только для Шины хранения");
			busSameChunkMessage = cfg.getBoolean("busSameChunkMessage", c, busSameChunkMessage, "Отправка сообщений ближайшим игрокам, если шина и блок находятся в разных чанках");

			cfg.save();
		}
		catch (final Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static final boolean inList(Set<String> list, ItemStack stack)
	{
		return inList(list, stack.getItem(), stack.getItemDamage());
	}

	public static final boolean inList(Set<String> list, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inList(list, ((ItemBlock) item).field_150939_a, meta);

		return inList(list, getId(item), meta);
	}

	public static final boolean inList(Set<String> list, Block block, int meta)
	{
		return inList(list, getId(block), meta);
	}

	private static final boolean inList(Set<String> list, String id, int meta)
	{
		return id != null && (list.contains(id) || list.contains(id + ':' + meta));
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

	private static final String getId(Item item)
	{
		return GameData	.getItemRegistry()
						.getNameForObject(item);
	}

	private static final String getId(Block block)
	{
		return GameData	.getBlockRegistry()
						.getNameForObject(block);
	}
}
