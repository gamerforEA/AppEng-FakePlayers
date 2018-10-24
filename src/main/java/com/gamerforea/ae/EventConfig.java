package com.gamerforea.ae;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import java.util.Set;

public final class EventConfig
{
	private static final String[] DEFAULT_BLOCKS = { "minecraft:bedrock", "modid:blockname:meta" };
	public static final Set<String> pilonBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> formationPlaneBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> annihilationPlaneBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> autoCraftBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> autoCraftForceCheckList = Sets.newHashSet("Botania:blackHoleTalisman", "ThaumicTinkerer:blockTalisman");
	public static final Set<String> busBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static String securityBypassPermission = "appeng.security.bypass";
	public static float chargedStaffDamage = 6F;
	public static boolean chargedStaffFix = true;
	public static int autoCraftFixMode = 0;
	public static boolean clearInvOnBreak = true;
	public static boolean annihilationPlaneNoBreakInv = true;
	public static boolean guiOnePlayer = false;

	public static boolean busSameChunk = false;
	public static boolean busSameChunkStorageOnly = false;
	public static boolean busSameChunkMessage = false;
	public static boolean experimentalChunkDupeFix = false;
	public static boolean fastChunkDupeFix = false;

	public static boolean craftingUnitBlockYOnly = false;
	public static boolean doubleChestDupeFix = true;
	public static boolean globalChunkDupeFix = true;

	public static boolean useTreeItemList = false;
	public static int maxTimesToCraft = Integer.MAX_VALUE;
	public static int craftTermCooldown = 0;

	public static boolean denySplitCraftingUnitsByChunks = false;
	public static boolean chestDriveDenyAutoInsertExtract = true;

	static
	{
		init();
	}

	public static void init()
	{
		try
		{
			final Configuration cfg = FastUtils.getConfig("AppEng");
			String c = Configuration.CATEGORY_GENERAL;

			readStringSet(cfg, "pilonBlackList", c, "Чёрный список блоков для Пилонов", pilonBlackList);
			readStringSet(cfg, "formationPlaneBlackList", c, "Чёрный список блоков для Плоскости формирования", formationPlaneBlackList);
			readStringSet(cfg, "annihilationPlaneBlackList", c, "Чёрный список блоков для Плоскости истребления", annihilationPlaneBlackList);
			readStringSet(cfg, "autoCraftBlackList", c, "Чёрный список блоков для автокрафта", autoCraftBlackList);
			readStringSet(cfg, "autoCraftForceCheckList", c, "Список предметов с принудительной проверкой NBT для автокрафта", autoCraftForceCheckList);
			readStringSet(cfg, "busBlackList", c, "Чёрный список блоков для шин импорта/экспорта и интерфейсов", busBlackList);
			securityBypassPermission = cfg.getString("securityBypassPermission", c, securityBypassPermission, "Permission для игнорирования защиты AE2-сети");
			chargedStaffDamage = cfg.getFloat("chargedStaffDamage", c, chargedStaffDamage, 0, Float.MAX_VALUE, "Урон Заряженного посоха");
			chargedStaffFix = cfg.getBoolean("chargedStaffFix", c, chargedStaffFix, "Исправление Зарженного посоха");
			autoCraftFixMode = cfg.getInt("autoCraftFixMode", c, autoCraftFixMode, 0, 2, "Режим фикса дюпа с автокрафтом [0 - старый фикс; 1 - ненадёжный фикс; 2 - экспериментальный фикс]");
			clearInvOnBreak = cfg.getBoolean("clearInvOnBreak", c, clearInvOnBreak, "Очистка инвентаря блока при его разрушении (защита от дюпа)");
			annihilationPlaneNoBreakInv = cfg.getBoolean("annihilationPlaneNoBreakInv", c, annihilationPlaneNoBreakInv, "Плоскость истребления не может ломать блоки с инвентарями");
			guiOnePlayer = cfg.getBoolean("guiOnePlayer", c, guiOnePlayer, "GUI может открыть только один игрок одновременно");

			busSameChunk = cfg.getBoolean("busSameChunk", c, busSameChunk, "Шины работают с блоками, только если находятся в одном чанке");
			busSameChunkStorageOnly = cfg.getBoolean("busSameChunkStorageOnly", c, busSameChunkStorageOnly, "Проверять чанки только для Шины хранения");
			busSameChunkMessage = cfg.getBoolean("busSameChunkMessage", c, busSameChunkMessage, "Отправка сообщений ближайшим игрокам, если шина и блок находятся в разных чанках");
			experimentalChunkDupeFix = cfg.getBoolean("experimentalChunkDupeFix", c, experimentalChunkDupeFix, "Экспериментальный фикс дюпа с чанками и шинами");
			fastChunkDupeFix = cfg.getBoolean("fastChunkDupeFix", c, fastChunkDupeFix, "Использовать более оптимальный алгоритм фикса дюпа с чанками (может снизить надёжность)");

			craftingUnitBlockYOnly = cfg.getBoolean("craftingUnitBlockYOnly", c, craftingUnitBlockYOnly, "Блоки создания (монитор, хранилище, *обработки) в многоблочной структуре должны располагаться только по вертикали (защита от дюпа с выгрузкой чанков)");
			doubleChestDupeFix = cfg.getBoolean("doubleChestDupeFix", c, doubleChestDupeFix, "Фикс дюпа с Шинами хранения и двойными сундуками");
			globalChunkDupeFix = cfg.getBoolean("globalChunkDupeFix", c, globalChunkDupeFix, "Глобальный фикс дюпа с выгрузкой чанков (может несколько снизиить производительность) (можно выключить experimentalChunkDupeFix и doubleChestDupeFix)");

			useTreeItemList = cfg.getBoolean("useTreeItemList", c, useTreeItemList, "Использовать синхронизированный TreeMap вместо ConcurrentSkipListMap в ItemList (может повысить производительность) (небезопасно)");
			maxTimesToCraft = cfg.getInt("maxTimesToCraft", c, maxTimesToCraft, 1, Integer.MAX_VALUE, "Максимальное количество попыток крафта предмета за один клик в Терминале создания");
			craftTermCooldown = cfg.getInt("craftTermCooldown", c, craftTermCooldown, 0, Integer.MAX_VALUE, "Кулдаун для попыток крафта предмета в Терминале создания (в тиках)");

			denySplitCraftingUnitsByChunks = cfg.getBoolean("denySplitCraftingUnitsByChunks", c, denySplitCraftingUnitsByChunks, "Запретить распределять структуру из блоков создания по нескольким чанкам (защита от дюпа)");
			chestDriveDenyAutoInsertExtract = cfg.getBoolean("chestDriveDenyAutoInsertExtract", c, chestDriveDenyAutoInsertExtract, "Запретить вставлять и извлекать ячейки в МЭ Сундуки и МЭ Накопители с помощью труб, шин и пр.");

			cfg.save();
		}
		catch (final Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static boolean inList(Set<String> list, ItemStack stack)
	{
		return inList(list, stack.getItem(), stack.getItemDamage());
	}

	public static boolean inList(Set<String> list, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inList(list, ((ItemBlock) item).field_150939_a, meta);

		return inList(list, getId(item), meta);
	}

	public static boolean inList(Set<String> list, Block block, int meta)
	{
		return inList(list, getId(block), meta);
	}

	private static boolean inList(Set<String> list, String id, int meta)
	{
		return id != null && (list.contains(id) || list.contains(id + ':' + meta));
	}

	private static void readStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		final Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static Set<String> getStringSet(final Configuration cfg, final String name, final String category, final String comment, final Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[0]));
	}

	private static Set<String> getStringSet(final Configuration cfg, final String name, final String category, final String comment, final String... def)
	{
		return Sets.newHashSet(cfg.getStringList(name, category, def, comment));
	}

	private static String getId(Item item)
	{
		return GameData.getItemRegistry().getNameForObject(item);
	}

	private static String getId(Block block)
	{
		return GameData.getBlockRegistry().getNameForObject(block);
	}
}
