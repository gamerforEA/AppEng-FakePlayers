package com.gamerforea.ae;

import com.gamerforea.eventhelper.config.*;
import net.minecraftforge.common.config.Configuration;

import java.util.Arrays;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

@Config(name = "AppEng")
public final class EventConfig
{
	private static final String CATEGORY_BLACKLISTS = "other";
	private static final String CATEGORY_PERMISSION = "permission";
	private static final String CATEGORY_OTHER = "other";
	private static final String CATEGORY_OTHER_OLD_CHUNK_FIX = CATEGORY_OTHER + Configuration.CATEGORY_SPLITTER + "oldChunkFix";
	private static final String CATEGORY_OTHER_STRICT = CATEGORY_OTHER + Configuration.CATEGORY_SPLITTER + "strict";
	private static final String CATEGORY_PERFORMANCE = "performance";
	private static final String CATEGORY_DEBUG = "debug";

	@ConfigItemBlockList(name = "pilon",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков для Пилонов",
						 oldName = "pilonBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList pilonBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "formationPlane",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков для Плоскости формирования",
						 oldName = "formationPlaneBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList formationPlaneBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "annihilationPlane",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков для Плоскости истребления",
						 oldName = "annihilationPlaneBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList annihilationPlaneBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "autoCraft",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков/предметов для автокрафта",
						 oldName = "autoCraftBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList autoCraftBlackList = new ItemBlockList();

	@ConfigItemBlockList(category = CATEGORY_BLACKLISTS,
						 comment = "Список предметов с принудительной проверкой NBT для автокрафта",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList autoCraftForceCheckList = new ItemBlockList();

	@ConfigItemBlockList(name = "bus",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков для шин импорта/экспорта и интерфейсов",
						 oldName = "busBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList busBlackList = new ItemBlockList();

	@ConfigString(category = CATEGORY_PERMISSION,
				  comment = "Permission для игнорирования защиты AE2-сети",
				  oldCategory = CATEGORY_GENERAL)
	public static String securityBypassPermission = "appeng.security.bypass";

	@ConfigBoolean(category = CATEGORY_PERMISSION,
				   comment = "Всегда разрешать BUILD при активном Терминале безопасности ('фикс' дюпов и багов)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean securityForceAllowBuild = false;

	@ConfigBoolean(category = CATEGORY_OTHER_OLD_CHUNK_FIX,
				   comment = "Шины работают с блоками, только если находятся в одном чанке",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean busSameChunk = false;

	@ConfigBoolean(category = CATEGORY_OTHER_OLD_CHUNK_FIX,
				   comment = "Проверять чанки только для Шины хранения",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean busSameChunkStorageOnly = false;

	@ConfigBoolean(category = CATEGORY_OTHER_OLD_CHUNK_FIX,
				   comment = "Отправка сообщений ближайшим игрокам, если шина и блок находятся в разных чанках",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean busSameChunkMessage = false;

	@ConfigBoolean(category = CATEGORY_OTHER_OLD_CHUNK_FIX,
				   comment = "Экспериментальный фикс дюпа с чанками и шинами",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean experimentalChunkDupeFix = false;

	@ConfigBoolean(category = CATEGORY_OTHER_OLD_CHUNK_FIX,
				   comment = "Использовать более оптимальный алгоритм фикса дюпа с чанками (может снизить надёжность)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean fastChunkDupeFix = false;

	@ConfigFloat(category = CATEGORY_OTHER,
				 comment = "Урон Заряженного посоха",
				 min = 0,
				 oldCategory = CATEGORY_GENERAL)
	public static float chargedStaffDamage = 6F;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Исправление Заряженного посоха",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean chargedStaffFix = true;

	@ConfigInt(category = CATEGORY_OTHER,
			   comment = "Режим фикса дюпа с автокрафтом [0 - старый фикс; 1 - ненадёжный фикс; 2 - экспериментальный фикс]",
			   min = 0,
			   max = 2,
			   oldCategory = CATEGORY_GENERAL)
	public static int autoCraftFixMode = 0;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Очистка инвентаря блока при его разрушении (защита от дюпа)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean clearInvOnBreak = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Плоскость истребления не может ломать блоки с инвентарями",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean annihilationPlaneNoBreakInv = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "GUI может открыть только один игрок одновременно",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean guiOnePlayer = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Блоки создания (монитор, хранилище, *обработки) в многоблочной структуре должны располагаться только по вертикали (защита от дюпа с выгрузкой чанков)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean craftingUnitBlockYOnly = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Фикс дюпа с Шинами хранения и двойными сундуками",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean doubleChestDupeFix = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Глобальный фикс дюпа с выгрузкой чанков (может несколько снизиить производительность) (можно выключить experimentalChunkDupeFix и doubleChestDupeFix)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean globalChunkDupeFix = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запретить распределять структуру из блоков создания по нескольким чанкам (защита от дюпа)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean denySplitCraftingUnitsByChunks = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запретить вставлять и извлекать ячейки в МЭ Сундуки и МЭ Накопители с помощью труб, шин и пр.",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean chestDriveDenyAutoInsertExtract = true;

	@ConfigInt(category = CATEGORY_PERFORMANCE,
			   comment = "Максимальное количество попыток крафта предмета за один клик в Терминале создания",
			   min = 1,
			   oldCategory = CATEGORY_GENERAL)
	public static int maxTimesToCraft = Integer.MAX_VALUE;

	@ConfigInt(category = CATEGORY_PERFORMANCE,
			   comment = "Кулдаун для попыток крафта предмета в Терминале создания (в тиках)",
			   min = 0,
			   oldCategory = CATEGORY_GENERAL)
	public static int craftTermCooldown = 0;

	@ConfigBoolean(category = CATEGORY_PERFORMANCE,
				   comment = "Использовать сочетание ConcurrentSkipListMap и ConcurrentHashMap в ItemList (может повысить производительность) (небезопасно - возможна 'рассинхронизация' этих двух коллекций)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean useHybridItemList = false;

	@ConfigBoolean(category = CATEGORY_PERFORMANCE,
				   comment = "Оптимизировать обновление содержимого МЭ-сети (небезопасно) (не рекомендуется)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean optimizeNetworkPostChange = false;

	@ConfigBoolean(category = CATEGORY_PERFORMANCE,
				   comment = "Предотвращать копирование NetworkList, если возможно (может повысить производительность при загрузки/выгрузке чанков в МЭ-сетью) (небезопасно)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean preventCopyOnWriteNetworkList = false;

	@ConfigBoolean(category = CATEGORY_PERFORMANCE,
				   comment = "Оптимизировать обработку энергосетей",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean optimizeEnergyGridCache = false;

	@ConfigBoolean(category = CATEGORY_PERFORMANCE,
				   comment = "Спекулятивные оптимизации (предполагается, что работа с оптимизированным кодом будет следовать предусмотренной логике [в нормальных условиях рост производительности с сохранением оригинальной логики, но в нестандартных случаях могут быть проблемы])",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean speculativeOptimizations = true;

	@ConfigBoolean(category = CATEGORY_PERFORMANCE,
				   comment = "Выключить перебор всех рецептов при несовпадении шаблона (может значительно повысить производительность Молекулярного сборщика) (некоторые рецепты могут перестать работать в Молекулярном сборщике)")
	public static boolean disableRecipeFallback = false;

	@ConfigBoolean(category = CATEGORY_OTHER_STRICT,
				   comment = "Фикс обновления сети автокрафта (небезопасно)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean fixCraftGridUpdating = false;

	@ConfigBoolean(category = CATEGORY_OTHER_STRICT,
				   comment = "Фикс ConcurrentModificationException при обновлении NetworkMonitor",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean fixNetworkListenersConcurrency = false;

	@ConfigBoolean(category = CATEGORY_OTHER_STRICT,
				   comment = "Строгое соблюдение контракта Read-Only классом ReadOnlyCollection (облегчает поиск ошибки)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean strictReadOnlyCollection = false;

	@ConfigBoolean(category = CATEGORY_DEBUG, comment = "Профилирование МЭ-сетей (команда '/ae2 gridperf')")
	public static boolean gridProfiling = false;

	public static void init()
	{
		ConfigUtils.readConfig(EventConfig.class);
	}

	static
	{
		autoCraftForceCheckList.addRaw(Arrays.asList("Botania:blackHoleTalisman", "ThaumicTinkerer:blockTalisman"));
		init();
	}
}
