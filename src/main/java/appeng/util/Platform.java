package appeng.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.AEItemDefinition;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.GuiHostType;
import appeng.hooks.TickHandler;
import appeng.integration.IntegrationType;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.item.AEItemStack;
import appeng.util.item.AESharedNBT;
import appeng.util.item.OreHelper;
import appeng.util.item.OreRefrence;
import appeng.util.prioitylist.IPartitionList;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Platform
{
	public static Block air = Blocks.air;
	public static final int DEF_OFFSET = 16;
	private static Random rdnSrc = new Random();
	private static Field tagList;
	private static WeakHashMap<World, EntityPlayer> fakePlayers = new WeakHashMap();
	private static Class Playerinstance;
	private static Method getOrCreateChunkWatcher;
	private static Method sendToAllPlayersWatchingChunk;

	public static Random getRandom()
	{
		return rdnSrc;
	}

	public static int getRandomInt()
	{
		return Math.abs(rdnSrc.nextInt());
	}

	public static float getRandomFloat()
	{
		return rdnSrc.nextFloat();
	}

	public static String formatPowerLong(long n, boolean isRate)
	{
		double p = (double) n / 100.0D;
		PowerUnits displayUnits = AEConfig.instance.selectedPowerUnit();
		p = PowerUnits.AE.convertTo(displayUnits, p);
		int offset = 0;
		String Lvl = "";
		String[] preFixes = new String[] { "k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y" };
		String unitName = displayUnits.name();
		if (displayUnits == PowerUnits.WA)
		{
			unitName = "J";
		}

		if (displayUnits == PowerUnits.MK)
		{
			unitName = "J";
		}

		while (p > 1000.0D && offset < preFixes.length)
		{
			p /= 1000.0D;
			Lvl = preFixes[offset];
			++offset;
		}

		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(p) + " " + Lvl + unitName + (isRate ? "/t" : "");
	}

	public static ForgeDirection crossProduct(ForgeDirection forward, ForgeDirection up)
	{
		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;
		switch (west_x + west_y * 2 + west_z * 3)
		{
			case -3:
				return ForgeDirection.NORTH;
			case -2:
				return ForgeDirection.DOWN;
			case -1:
				return ForgeDirection.WEST;
			case 0:
			default:
				return ForgeDirection.UNKNOWN;
			case 1:
				return ForgeDirection.EAST;
			case 2:
				return ForgeDirection.UP;
			case 3:
				return ForgeDirection.SOUTH;
		}
	}

	public static <T extends Enum> T nextEnum(T ce)
	{
		EnumSet valList = EnumSet.allOf(ce.getClass());
		int pLoc = ce.ordinal() + 1;
		if (pLoc >= valList.size())
		{
			pLoc = 0;
		}

		if (pLoc < 0 || pLoc >= valList.size())
		{
			pLoc = 0;
		}

		int pos = 0;
		Iterator i$ = valList.iterator();

		Object g;
		do
		{
			if (!i$.hasNext())
			{
				return null;
			}

			g = i$.next();
		}
		while (pos++ != pLoc);

		return (T) g;
	}

	public static <T extends Enum> T rotateEnum(T ce, boolean backwards, EnumSet ValidOptions)
	{
		do
		{
			if (backwards)
			{
				ce = prevEnum(ce);
			}
			else
			{
				ce = nextEnum(ce);
			}
		}
		while (!ValidOptions.contains(ce) || isNotValidSetting(ce));

		return ce;
	}

	private static boolean isNotValidSetting(Enum e)
	{
		return e == SortOrder.INVTWEAKS && !AppEng.instance.isIntegrationEnabled(IntegrationType.InvTweaks) ? true : (e == SearchBoxMode.NEI_AUTOSEARCH && !AppEng.instance.isIntegrationEnabled(IntegrationType.NEI) ? true : e == SearchBoxMode.NEI_MANUAL_SEARCH && !AppEng.instance.isIntegrationEnabled(IntegrationType.NEI));
	}

	public static <T extends Enum> T prevEnum(T ce)
	{
		EnumSet valList = EnumSet.allOf(ce.getClass());
		int pLoc = ce.ordinal() - 1;
		if (pLoc < 0)
		{
			pLoc = valList.size() - 1;
		}

		if (pLoc < 0 || pLoc >= valList.size())
		{
			pLoc = 0;
		}

		int pos = 0;
		Iterator i$ = valList.iterator();

		Object g;
		do
		{
			if (!i$.hasNext())
			{
				return null;
			}

			g = i$.next();
		}
		while (pos++ != pLoc);

		return (T) g;
	}

	public static boolean isClient()
	{
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

	public static boolean isServer()
	{
		return FMLCommonHandler.instance().getEffectiveSide().isServer();
	}

	public static void openGUI(EntityPlayer p, TileEntity tile, ForgeDirection side, GuiBridge type)
	{
		if (!isClient())
		{
			int x = (int) p.posX;
			int y = (int) p.posY;
			int z = (int) p.posZ;
			if (tile != null)
			{
				x = tile.xCoord;
				y = tile.yCoord;
				z = tile.zCoord;
			}

			if (type.getType().isItem() && tile == null || type.hasPermissions(tile, x, y, z, side, p))
			{
				if (tile != null && type.getType() != GuiHostType.ITEM)
				{
					p.openGui(AppEng.instance, type.ordinal() << 4 | side.ordinal(), tile.getWorldObj(), x, y, z);
				}
				else
				{
					p.openGui(AppEng.instance, type.ordinal() << 4 | 8, p.getEntityWorld(), x, y, z);
				}
			}

		}
	}

	public static boolean hasPermissions(DimensionalCoord dc, EntityPlayer player)
	{
		// TODO gamerforEA code start
		if (callBlockBreakEvent(dc.x, dc.y, dc.z, dc.getWorld(), player)) return false;
		// TODO gamerforEA code end

		return dc.getWorld().canMineBlock(player, dc.x, dc.y, dc.z);
	}

	public static boolean isBlockAir(World w, int x, int y, int z)
	{
		try
		{
			return w.getBlock(x, y, z).isAir(w, x, y, z);
		}
		catch (Throwable var5)
		{
			return false;
		}
	}

	public static boolean sameStackStags(ItemStack a, ItemStack b)
	{
		if (a == null && b == null)
		{
			return true;
		}
		else if (a != null && b != null)
		{
			if (a == b)
			{
				return true;
			}
			else
			{
				NBTTagCompound ta = a.getTagCompound();
				NBTTagCompound tb = b.getTagCompound();
				return ta == tb ? true : ((ta != null || tb != null) && (ta == null || !ta.hasNoTags() || tb != null) && (tb == null || !tb.hasNoTags() || ta != null) && (ta == null || !ta.hasNoTags() || tb == null || !tb.hasNoTags()) ? ((ta != null || tb == null) && (ta == null || tb != null) ? (AESharedNBT.isShared(ta) && AESharedNBT.isShared(tb) ? ta == tb : NBTEqualityTest(ta, tb)) : false) : true);
			}
		}
		else
		{
			return false;
		}
	}

	public static boolean NBTEqualityTest(NBTBase A, NBTBase B)
	{
		byte id = A.getId();
		if (id == B.getId())
		{
			switch (id)
			{
				case 1:
					return ((NBTTagByte) A).func_150287_d() == ((NBTTagByte) B).func_150287_d();
				case 2:
				case 7:
				default:
					return A.equals(B);
				case 3:
					return ((NBTTagInt) A).func_150287_d() == ((NBTTagInt) B).func_150287_d();
				case 4:
					return ((NBTTagLong) A).func_150291_c() == ((NBTTagLong) B).func_150291_c();
				case 5:
					return ((NBTTagFloat) A).func_150288_h() == ((NBTTagFloat) B).func_150288_h();
				case 6:
					return ((NBTTagDouble) A).func_150286_g() == ((NBTTagDouble) B).func_150286_g();
				case 8:
					return ((NBTTagString) A).func_150285_a_() == ((NBTTagString) B).func_150285_a_() || ((NBTTagString) A).func_150285_a_().equals(((NBTTagString) B).func_150285_a_());
				case 9:
					NBTTagList var11 = (NBTTagList) A;
					NBTTagList var12 = (NBTTagList) B;
					if (var11.tagCount() != var12.tagCount())
					{
						return false;
					}
					else
					{
						List var13 = tagList(var11);
						List var14 = tagList(var12);
						if (var13.size() != var14.size())
						{
							return false;
						}
						else
						{
							for (int var15 = 0; var15 < var13.size(); ++var15)
							{
								if (var14.get(var15) == null)
								{
									return false;
								}

								if (!NBTEqualityTest((NBTBase) var13.get(var15), (NBTBase) var14.get(var15)))
								{
									return false;
								}
							}

							return true;
						}
					}
				case 10:
					NBTTagCompound lA = (NBTTagCompound) A;
					NBTTagCompound lB = (NBTTagCompound) B;
					Set tag = lA.func_150296_c();
					Set aTag = lB.func_150296_c();
					if (tag.size() != aTag.size())
					{
						return false;
					}
					else
					{
						Iterator x = tag.iterator();

						NBTBase tag1;
						NBTBase aTag1;
						do
						{
							if (!x.hasNext())
							{
								return true;
							}

							String name = (String) x.next();
							tag1 = lA.getTag(name);
							aTag1 = lB.getTag(name);
							if (aTag1 == null)
							{
								return false;
							}
						}
						while (NBTEqualityTest(tag1, aTag1));

						return false;
					}
			}
		}
		else
		{
			return false;
		}
	}

	private static List<NBTBase> tagList(NBTTagList lB)
	{
		if (tagList == null)
		{
			try
			{
				tagList = lB.getClass().getDeclaredField("tagList");
			}
			catch (Throwable var5)
			{
				try
				{
					tagList = lB.getClass().getDeclaredField("field_74747_a");
				}
				catch (Throwable var4)
				{
					AELog.error(var5);
					AELog.error(var4);
				}
			}
		}

		try
		{
			tagList.setAccessible(true);
			return (List) tagList.get(lB);
		}
		catch (Throwable var3)
		{
			AELog.error(var3);
			return new ArrayList();
		}
	}

	public static int NBTOrderlessHash(NBTBase A)
	{
		byte hash = 0;
		byte id = A.getId();
		int var7 = hash + id;
		switch (id)
		{
			case 1:
				return var7 + ((NBTTagByte) A).func_150290_f();
			case 2:
			case 7:
			default:
				return var7;
			case 3:
				return var7 + ((NBTTagInt) A).func_150287_d();
			case 4:
				return var7 + (int) ((NBTTagLong) A).func_150291_c();
			case 5:
				return var7 + (int) ((NBTTagFloat) A).func_150288_h();
			case 6:
				return var7 + (int) ((NBTTagDouble) A).func_150286_g();
			case 8:
				return var7 + ((NBTTagString) A).func_150285_a_().hashCode();
			case 9:
				NBTTagList var8 = (NBTTagList) A;
				var7 += 9 * var8.tagCount();
				List var9 = tagList(var8);

				for (int var10 = 0; var10 < var9.size(); ++var10)
				{
					var7 += Integer.valueOf(var10).hashCode() ^ NBTOrderlessHash((NBTBase) var9.get(var10));
				}

				return var7;
			case 10:
				NBTTagCompound lA = (NBTTagCompound) A;
				Set l = lA.func_150296_c();

				String name;
				for (Iterator x = l.iterator(); x.hasNext(); var7 += name.hashCode() ^ NBTOrderlessHash(lA.getTag(name)))
				{
					name = (String) x.next();
				}

				return var7;
		}
	}

	public static IRecipe findMatchingRecipe(InventoryCrafting par1InventoryCrafting, World par2World)
	{
		CraftingManager cm = CraftingManager.getInstance();
		List rl = cm.getRecipeList();

		for (int x = 0; x < rl.size(); ++x)
		{
			IRecipe r = (IRecipe) rl.get(x);
			if (r.matches(par1InventoryCrafting, par2World))
			{
				return r;
			}
		}

		return null;
	}

	public static ItemStack[] getBlockDrops(World w, int x, int y, int z)
	{
		ArrayList out = new ArrayList();
		Block which = w.getBlock(x, y, z);
		if (which != null)
		{
			out = which.getDrops(w, x, y, z, w.getBlockMetadata(x, y, z), 0);
		}

		return out == null ? new ItemStack[0] : (ItemStack[]) out.toArray(new ItemStack[out.size()]);
	}

	public static ForgeDirection cycleOrientations(ForgeDirection dir, boolean upAndDown)
	{
		if (upAndDown)
		{
			switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[dir.ordinal()])
			{
				case 1:
					return ForgeDirection.SOUTH;
				case 2:
					return ForgeDirection.EAST;
				case 3:
					return ForgeDirection.WEST;
				case 4:
					return ForgeDirection.NORTH;
				case 5:
					return ForgeDirection.UP;
				case 6:
					return ForgeDirection.DOWN;
				case 7:
					return ForgeDirection.UNKNOWN;
			}
		}
		else
		{
			switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[dir.ordinal()])
			{
				case 1:
					return ForgeDirection.SOUTH;
				case 2:
					return ForgeDirection.EAST;
				case 3:
					return ForgeDirection.WEST;
				case 4:
					return ForgeDirection.UP;
				case 5:
					return ForgeDirection.DOWN;
				case 6:
					return ForgeDirection.NORTH;
				case 7:
					return ForgeDirection.UNKNOWN;
			}
		}

		return ForgeDirection.UNKNOWN;
	}

	public static NBTTagCompound openNbtData(ItemStack i)
	{
		NBTTagCompound compound = i.getTagCompound();
		if (compound == null)
		{
			i.setTagCompound(compound = new NBTTagCompound());
		}

		return compound;
	}

	public static void spawnDrops(World w, int x, int y, int z, List<ItemStack> drops)
	{
		if (isServer())
		{
			Iterator i$ = drops.iterator();

			while (i$.hasNext())
			{
				ItemStack i = (ItemStack) i$.next();
				if (i != null && i.stackSize > 0)
				{
					double offset_x = (double) ((getRandomInt() % 32 - 16) / 82);
					double offset_y = (double) ((getRandomInt() % 32 - 16) / 82);
					double offset_z = (double) ((getRandomInt() % 32 - 16) / 82);
					EntityItem ei = new EntityItem(w, 0.5D + offset_x + (double) x, 0.5D + offset_y + (double) y, 0.2D + offset_z + (double) z, i.copy());
					w.spawnEntityInWorld(ei);
				}
			}
		}

	}

	public static IInventory GetChestInv(Object te)
	{
		TileEntityChest teA = (TileEntityChest) te;
		Object teB = null;
		Block myBlockID = teA.getWorldObj().getBlock(teA.xCoord, teA.yCoord, teA.zCoord);
		if (teA.getWorldObj().getBlock(teA.xCoord + 1, teA.yCoord, teA.zCoord) == myBlockID)
		{
			teB = teA.getWorldObj().getTileEntity(teA.xCoord + 1, teA.yCoord, teA.zCoord);
			if (!(teB instanceof TileEntityChest))
			{
				teB = null;
			}
		}

		TileEntityChest x;
		TileEntity teB1;
		if (teB == null && teA.getWorldObj().getBlock(teA.xCoord - 1, teA.yCoord, teA.zCoord) == myBlockID)
		{
			teB1 = teA.getWorldObj().getTileEntity(teA.xCoord - 1, teA.yCoord, teA.zCoord);
			if (!(teB1 instanceof TileEntityChest))
			{
				teB = null;
			}
			else
			{
				x = teA;
				teA = (TileEntityChest) teB1;
				teB = x;
			}
		}

		if (teB == null && teA.getWorldObj().getBlock(teA.xCoord, teA.yCoord, teA.zCoord + 1) == myBlockID)
		{
			teB = teA.getWorldObj().getTileEntity(teA.xCoord, teA.yCoord, teA.zCoord + 1);
			if (!(teB instanceof TileEntityChest))
			{
				teB = null;
			}
		}

		if (teB == null && teA.getWorldObj().getBlock(teA.xCoord, teA.yCoord, teA.zCoord - 1) == myBlockID)
		{
			teB1 = teA.getWorldObj().getTileEntity(teA.xCoord, teA.yCoord, teA.zCoord - 1);
			if (!(teB1 instanceof TileEntityChest))
			{
				teB = null;
			}
			else
			{
				x = teA;
				teA = (TileEntityChest) teB1;
				teB = x;
			}
		}

		return (IInventory) (teB == null ? teA : new InventoryLargeChest("", teA, (TileEntityChest) teB));
	}

	public static boolean isModLoaded(String modid)
	{
		try
		{
			return Loader.isModLoaded(modid);
		}
		catch (Throwable var3)
		{
			Iterator i$ = Loader.instance().getActiveModList().iterator();

			ModContainer f;
			do
			{
				if (!i$.hasNext())
				{
					return false;
				}

				f = (ModContainer) i$.next();
			}
			while (!f.getModId().equals(modid));

			return true;
		}
	}

	public static ItemStack findMatchingRecipeOutput(InventoryCrafting ic, World worldObj)
	{
		return CraftingManager.getInstance().findMatchingRecipe(ic, worldObj);
	}

	@SideOnly(Side.CLIENT)
	public static List getTooltip(Object o)
	{
		if (o == null)
		{
			return new ArrayList();
		}
		else
		{
			ItemStack itemStack = null;
			if (o instanceof AEItemStack)
			{
				AEItemStack errB = (AEItemStack) o;
				return errB.getToolTip();
			}
			else if (o instanceof ItemStack)
			{
				itemStack = (ItemStack) o;

				try
				{
					return itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
				}
				catch (Exception var3)
				{
					return new ArrayList();
				}
			}
			else
			{
				return new ArrayList();
			}
		}
	}

	public static String getModId(IAEItemStack is)
	{
		if (is == null)
		{
			return "** Null";
		}
		else
		{
			String n = ((AEItemStack) is).getModID();
			return n == null ? "** Null" : n;
		}
	}

	public static String getItemDisplayName(Object o)
	{
		if (o == null)
		{
			return "** Null";
		}
		else
		{
			ItemStack itemStack = null;
			String errA;
			if (o instanceof AEItemStack)
			{
				errA = ((AEItemStack) o).getDisplayName();
				return errA == null ? "** Null" : errA;
			}
			else if (o instanceof ItemStack)
			{
				itemStack = (ItemStack) o;

				try
				{
					errA = itemStack.getDisplayName();
					if (errA == null || errA.equals(""))
					{
						errA = itemStack.getItem().getUnlocalizedName(itemStack);
					}

					return errA == null ? "** Null" : errA;
				}
				catch (Exception var5)
				{
					try
					{
						String errB = itemStack.getUnlocalizedName();
						return errB == null ? "** Null" : errB;
					}
					catch (Exception var4)
					{
						return "** Exception";
					}
				}
			}
			else
			{
				return "**Invalid Object";
			}
		}
	}

	public static boolean hasSpecialComparison(IAEItemStack willAdd)
	{
		if (willAdd == null)
		{
			return false;
		}
		else
		{
			IAETagCompound tag = willAdd.getTagCompound();
			return tag != null && ((AESharedNBT) tag).getSpecialComparison() != null;
		}
	}

	public static boolean hasSpecialComparison(ItemStack willAdd)
	{
		return AESharedNBT.isShared(willAdd.getTagCompound()) && ((AESharedNBT) willAdd.getTagCompound()).getSpecialComparison() != null;
	}

	public static boolean isWrench(EntityPlayer player, ItemStack eq, int x, int y, int z)
	{
		if (eq != null)
		{
			// TODO gamerforEA code start
			if (callBlockBreakEvent(x, y, z, player.worldObj, player)) return false;
			// TODO gamerforEA code end

			try
			{
				if (eq.getItem() instanceof IToolWrench)
				{
					IToolWrench wrench = (IToolWrench) eq.getItem();
					return wrench.canWrench(player, x, y, z);
				}
			}
			catch (Throwable var6)
			{
				;
			}

			if (eq.getItem() instanceof IAEWrench)
			{
				IAEWrench wrench = (IAEWrench) eq.getItem();
				return wrench.canWrench(eq, player, x, y, z);
			}
		}

		return false;
	}

	public static boolean isChargeable(ItemStack i)
	{
		if (i == null)
		{
			return false;
		}
		else
		{
			Item it = i.getItem();
			return it instanceof IAEItemPowerStorage ? ((IAEItemPowerStorage) it).getPowerFlow(i) != AccessRestriction.READ : false;
		}
	}

	public static EntityPlayer getPlayer(WorldServer w)
	{
		if (w == null)
		{
			throw new NullPointerException();
		}
		else
		{
			EntityPlayer wrp = (EntityPlayer) fakePlayers.get(w);
			if (wrp != null)
			{
				return wrp;
			}
			else
			{
				FakePlayer p = FakePlayerFactory.getMinecraft(w);
				fakePlayers.put(w, p);
				return p;
			}
		}
	}

	public static int MC2MEColor(int color)
	{
		switch (color)
		{
			case 0:
				return 1;
			case 1:
				return 4;
			case 2:
				return 6;
			case 3:
				return 3;
			case 4:
				return 0;
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 12:
			case 13:
			case 14:
			default:
				return -1;
			case 11:
				return 5;
			case 15:
				return 2;
		}
	}

	public static int findEmpty(Object[] l)
	{
		for (int x = 0; x < l.length; ++x)
		{
			if (l[x] == null)
			{
				return x;
			}
		}

		return -1;
	}

	public static <T> T pickRandom(Collection<T> outs)
	{
		int index = rdnSrc.nextInt(outs.size());
		Iterator i = outs.iterator();

		while (i.hasNext() && index-- > 0)
		{
			i.next();
		}

		return (T) (i.hasNext() ? i.next() : null);
	}

	public static boolean blockAtLocationIs(IBlockAccess w, int x, int y, int z, AEItemDefinition def)
	{
		return def.block() == w.getBlock(x, y, z);
	}

	public static ForgeDirection rotateAround(ForgeDirection forward, ForgeDirection axis)
	{
		if (axis != ForgeDirection.UNKNOWN && forward != ForgeDirection.UNKNOWN)
		{
			switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[forward.ordinal()])
			{
				case 1:
					switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[axis.ordinal()])
					{
						case 3:
							return ForgeDirection.UP;
						case 4:
							return ForgeDirection.DOWN;
						case 5:
							return ForgeDirection.WEST;
						case 6:
							return ForgeDirection.EAST;
						default:
							return forward;
					}
				case 2:
					switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[axis.ordinal()])
					{
						case 3:
							return ForgeDirection.DOWN;
						case 4:
							return ForgeDirection.UP;
						case 5:
							return ForgeDirection.EAST;
						case 6:
							return ForgeDirection.WEST;
						default:
							return forward;
					}
				case 3:
					switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[axis.ordinal()])
					{
						case 1:
							return ForgeDirection.UP;
						case 2:
							return ForgeDirection.DOWN;
						case 3:
						case 4:
						default:
							break;
						case 5:
							return ForgeDirection.NORTH;
						case 6:
							return ForgeDirection.SOUTH;
					}
				case 4:
					switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[axis.ordinal()])
					{
						case 1:
							return ForgeDirection.DOWN;
						case 2:
							return ForgeDirection.UP;
						case 3:
						case 4:
						default:
							return forward;
						case 5:
							return ForgeDirection.SOUTH;
						case 6:
							return ForgeDirection.NORTH;
					}
				case 5:
					switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[axis.ordinal()])
					{
						case 1:
							return ForgeDirection.WEST;
						case 2:
							return ForgeDirection.EAST;
						case 3:
							return ForgeDirection.SOUTH;
						case 4:
							return ForgeDirection.NORTH;
						default:
							return forward;
					}
				case 6:
					switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[axis.ordinal()])
					{
						case 1:
							return ForgeDirection.EAST;
						case 2:
							return ForgeDirection.WEST;
						case 3:
							return ForgeDirection.NORTH;
						case 4:
							return ForgeDirection.SOUTH;
						case 5:
							return forward;
						case 6:
							return forward;
					}
			}

			return forward;
		}
		else
		{
			return forward;
		}
	}

	@SideOnly(Side.CLIENT)
	public static String gui_localize(String string)
	{
		return StatCollector.translateToLocal(string);
	}

	public static boolean isSameItemType(ItemStack ol, ItemStack op)
	{
		return ol != null && op != null && ol.getItem() == op.getItem() ? (ol.isItemStackDamageable() ? true : ol.getItemDamage() == ol.getItemDamage()) : false;
	}

	public static boolean isSameItem(ItemStack ol, ItemStack op)
	{
		return ol != null && op != null && ol.isItemEqual(op);
	}

	public static ItemStack cloneItemStack(ItemStack a)
	{
		return a.copy();
	}

	public static boolean isSameItemPrecise(ItemStack is, ItemStack filter)
	{
		return isSameItem(is, filter) && sameStackStags(is, filter);
	}

	public static boolean isSameItemFuzzy(ItemStack a, ItemStack b, FuzzyMode Mode)
	{
		if (a == null && b == null)
		{
			return true;
		}
		else if (a == null)
		{
			return false;
		}
		else if (b == null)
		{
			return false;
		}
		else if (a.getItem() != null && b.getItem() != null && a.getItem().isDamageable() && a.getItem() == b.getItem())
		{
			float bOR1;
			try
			{
				if (Mode == FuzzyMode.IGNORE_ALL)
				{
					return true;
				}
				else if (Mode == FuzzyMode.PERCENT_99)
				{
					return a.getItemDamageForDisplay() > 1 == b.getItemDamageForDisplay() > 1;
				}
				else
				{
					float aOR1 = 1.0F - (float) a.getItemDamageForDisplay() / (float) a.getMaxDamage();
					bOR1 = 1.0F - (float) b.getItemDamageForDisplay() / (float) b.getMaxDamage();
					return aOR1 > Mode.breakPoint == bOR1 > Mode.breakPoint;
				}
			}
			catch (Throwable var6)
			{
				if (Mode == FuzzyMode.IGNORE_ALL)
				{
					return true;
				}
				else if (Mode == FuzzyMode.PERCENT_99)
				{
					return a.getItemDamage() > 1 == b.getItemDamage() > 1;
				}
				else
				{
					bOR1 = (float) a.getItemDamage() / (float) a.getMaxDamage();
					float BPercentDamaged = (float) b.getItemDamage() / (float) b.getMaxDamage();
					return bOR1 > Mode.breakPoint == BPercentDamaged > Mode.breakPoint;
				}
			}
		}
		else
		{
			OreRefrence aOR = OreHelper.instance.isOre(a);
			OreRefrence bOR = OreHelper.instance.isOre(b);
			return OreHelper.instance.sameOre(aOR, bOR) ? true : a.isItemEqual(b);
		}
	}

	public static LookDirection getPlayerRay(EntityPlayer player, float eyeoffset)
	{
		float f = 1.0F;
		float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
		double d1 = (double) eyeoffset;
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		if (player instanceof EntityPlayerMP)
		{
			d3 = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		}

		Vec3 vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
		return new LookDirection(vec3, vec31);
	}

	public static MovingObjectPosition rayTrace(EntityPlayer p, boolean hitBlocks, boolean hitEntities)
	{
		World w = p.getEntityWorld();
		float f = 1.0F;
		float f1 = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * f;
		float f2 = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * f;
		double d0 = p.prevPosX + (p.posX - p.prevPosX) * (double) f;
		double d1 = p.prevPosY + (p.posY - p.prevPosY) * (double) f + 1.62D - (double) p.yOffset;
		double d2 = p.prevPosZ + (p.posZ - p.prevPosZ) * (double) f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 32.0D;
		Vec3 vec31 = vec3.addVector((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(Math.min(vec3.xCoord, vec31.xCoord), Math.min(vec3.yCoord, vec31.yCoord), Math.min(vec3.zCoord, vec31.zCoord), Math.max(vec3.xCoord, vec31.xCoord), Math.max(vec3.yCoord, vec31.yCoord), Math.max(vec3.zCoord, vec31.zCoord)).expand(16.0D, 16.0D, 16.0D);
		Entity entity = null;
		double Closeest = 9999999.0D;
		if (hitEntities)
		{
			List pos = w.getEntitiesWithinAABBExcludingEntity(p, bb);

			for (int Srec = 0; Srec < pos.size(); ++Srec)
			{
				Entity entity1 = (Entity) pos.get(Srec);
				if (!entity1.isDead && entity1 != p && !(entity1 instanceof EntityItem) && entity1.isEntityAlive() && entity1.riddenByEntity != p)
				{
					f1 = 0.3F;
					AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand((double) f1, (double) f1, (double) f1);
					MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3, vec31);
					if (movingobjectposition1 != null)
					{
						double nd = vec3.squareDistanceTo(movingobjectposition1.hitVec);
						if (nd < Closeest)
						{
							entity = entity1;
							Closeest = nd;
						}
					}
				}
			}
		}

		MovingObjectPosition var34 = null;
		Vec3 var35 = null;
		if (hitBlocks)
		{
			var35 = Vec3.createVectorHelper(d0, d1, d2);
			var34 = w.rayTraceBlocks(vec3, vec31, true);
		}

		if (entity != null && var34 != null && var34.hitVec.squareDistanceTo(var35) > Closeest)
		{
			var34 = new MovingObjectPosition(entity);
		}
		else if (entity != null && var34 == null)
		{
			var34 = new MovingObjectPosition(entity);
		}

		return var34;
	}

	public static long nanoTime()
	{
		return 0L;
	}

	public static <StackType extends IAEStack> StackType poweredExtraction(IEnergySource energy, IMEInventory<StackType> cell, StackType request, BaseActionSource src)
	{
		IAEStack possible = cell.extractItems((StackType) request.copy(), Actionable.SIMULATE, src);
		long retrieved = 0L;
		if (possible != null)
		{
			retrieved = possible.getStackSize();
		}

		double availablePower = energy.extractAEPower((double) retrieved, Actionable.SIMULATE, PowerMultiplier.CONFIG);
		long itemToExtract = Math.min((long) (availablePower + 0.9D), retrieved);
		if (itemToExtract > 0L)
		{
			energy.extractAEPower((double) retrieved, Actionable.MODULATE, PowerMultiplier.CONFIG);
			possible.setStackSize(itemToExtract);
			IAEStack ret = cell.extractItems((StackType) possible, Actionable.MODULATE, src);
			return (StackType) ret;
		}
		else
		{
			return null;
		}
	}

	public static <StackType extends IAEStack> StackType poweredInsert(IEnergySource energy, IMEInventory<StackType> cell, StackType input, BaseActionSource src)
	{
		IAEStack possible = cell.injectItems((StackType) input.copy(), Actionable.SIMULATE, src);
		long stored = input.getStackSize();
		if (possible != null)
		{
			stored -= possible.getStackSize();
		}

		double availablePower = energy.extractAEPower((double) stored, Actionable.SIMULATE, PowerMultiplier.CONFIG);
		long itemToAdd = Math.min((long) (availablePower + 0.9D), stored);
		if (itemToAdd > 0L)
		{
			energy.extractAEPower((double) stored, Actionable.MODULATE, PowerMultiplier.CONFIG);
			IAEStack ret;
			if (itemToAdd < input.getStackSize())
			{
				ret = input.copy();
				ret.decStackSize(itemToAdd);
				input.setStackSize(itemToAdd);
				ret.add(cell.injectItems(input, Actionable.MODULATE, src));
				return (StackType) ret;
			}
			else
			{
				ret = cell.injectItems(input, Actionable.MODULATE, src);
				return (StackType) ret;
			}
		}
		else
		{
			return input;
		}
	}

	public static void postChanges(IStorageGrid gs, ItemStack removed, ItemStack added, BaseActionSource src)
	{
		IItemList itemChanges = AEApi.instance().storage().createItemList();
		IItemList fluidChanges = AEApi.instance().storage().createFluidList();
		IMEInventoryHandler myItems;
		IMEInventoryHandler myFluids1;
		if (removed != null)
		{
			myItems = AEApi.instance().registries().cell().getCellInventory(removed, (ISaveProvider) null, StorageChannel.ITEMS);
			if (myItems != null)
			{
				Iterator myFluids = myItems.getAvailableItems(itemChanges).iterator();

				while (myFluids.hasNext())
				{
					IAEItemStack i$ = (IAEItemStack) myFluids.next();
					i$.setStackSize(-i$.getStackSize());
				}
			}

			myFluids1 = AEApi.instance().registries().cell().getCellInventory(removed, (ISaveProvider) null, StorageChannel.FLUIDS);
			if (myFluids1 != null)
			{
				Iterator i$1 = myFluids1.getAvailableItems(fluidChanges).iterator();

				while (i$1.hasNext())
				{
					IAEFluidStack is = (IAEFluidStack) i$1.next();
					is.setStackSize(-is.getStackSize());
				}
			}
		}

		if (added != null)
		{
			myItems = AEApi.instance().registries().cell().getCellInventory(added, (ISaveProvider) null, StorageChannel.ITEMS);
			if (myItems != null)
			{
				myItems.getAvailableItems(itemChanges);
			}

			myFluids1 = AEApi.instance().registries().cell().getCellInventory(added, (ISaveProvider) null, StorageChannel.FLUIDS);
			if (myFluids1 != null)
			{
				myFluids1.getAvailableItems(fluidChanges);
			}
		}

		gs.postAlterationOfStoredItems(StorageChannel.ITEMS, itemChanges, src);
	}

	public static <T extends IAEStack<T>> void postListChanges(IItemList<T> before, IItemList<T> after, IMEMonitorHandlerReceiver<T> meMonitorPassthu, BaseActionSource source)
	{
		LinkedList changes = new LinkedList();
		Iterator i$ = before.iterator();

		IAEStack is;
		while (i$.hasNext())
		{
			is = (IAEStack) i$.next();
			is.setStackSize(-is.getStackSize());
		}

		i$ = after.iterator();

		while (i$.hasNext())
		{
			is = (IAEStack) i$.next();
			before.add((T) is);
		}

		i$ = before.iterator();

		while (i$.hasNext())
		{
			is = (IAEStack) i$.next();
			if (is.getStackSize() != 0L)
			{
				changes.add(is);
			}
		}

		if (!changes.isEmpty())
		{
			meMonitorPassthu.postChange((IBaseMonitor) null, changes, source);
		}

	}

	public static int generateTileHash(TileEntity target)
	{
		if (target == null)
		{
			return 0;
		}
		else
		{
			int hash = target.hashCode();
			if (target instanceof ITileStorageMonitorable)
			{
				return 0;
			}
			else
			{
				if (target instanceof TileEntityChest)
				{
					TileEntityChest arr$ = (TileEntityChest) target;
					arr$.checkForAdjacentChests();
					if (arr$.adjacentChestZNeg != null)
					{
						hash ^= arr$.adjacentChestZNeg.hashCode();
					}
					else if (arr$.adjacentChestZPos != null)
					{
						hash ^= arr$.adjacentChestZPos.hashCode();
					}
					else if (arr$.adjacentChestXPos != null)
					{
						hash ^= arr$.adjacentChestXPos.hashCode();
					}
					else if (arr$.adjacentChestXNeg != null)
					{
						hash ^= arr$.adjacentChestXNeg.hashCode();
					}
				}
				else if (target instanceof IInventory)
				{
					hash ^= ((IInventory) target).getSizeInventory();
					if (target instanceof ISidedInventory)
					{
						ForgeDirection[] var13 = ForgeDirection.VALID_DIRECTIONS;
						int len$ = var13.length;

						for (int i$ = 0; i$ < len$; ++i$)
						{
							ForgeDirection dir = var13[i$];
							int offset = 0;
							int[] sides = ((ISidedInventory) target).getAccessibleSlotsFromSide(dir.ordinal());
							if (sides == null)
							{
								return 0;
							}

							int[] arr$1 = sides;
							int len$1 = sides.length;

							for (int i$1 = 0; i$1 < len$1; ++i$1)
							{
								Integer Side = Integer.valueOf(arr$1[i$1]);
								int c = Side.intValue() << offset++ % 8 ^ 1 << dir.ordinal();
								hash = c + (hash << 6) + (hash << 16) - hash;
							}
						}
					}
				}

				return hash;
			}
		}
	}

	public static boolean securityCheck(GridNode a, GridNode b)
	{
		if (a.lastSecurityKey == -1L && b.lastSecurityKey == -1L)
		{
			return false;
		}
		else if (a.lastSecurityKey == b.lastSecurityKey)
		{
			return false;
		}
		else
		{
			boolean a_isSecure = isPowered(a.getGrid()) && a.lastSecurityKey != -1L;
			boolean b_isSecure = isPowered(b.getGrid()) && b.lastSecurityKey != -1L;
			if (AEConfig.instance.isFeatureEnabled(AEFeature.LogSecurityAudits))
			{
				AELog.info("Audit: " + a_isSecure + " : " + b_isSecure + " @ " + a.lastSecurityKey + " vs " + b.lastSecurityKey + " & " + a.playerID + " vs " + b.playerID, new Object[0]);
			}

			return a_isSecure && b_isSecure ? true : (!a_isSecure && b_isSecure ? checkPlayerPermissions(b.getGrid(), a.playerID) : (a_isSecure && !b_isSecure ? checkPlayerPermissions(a.getGrid(), b.playerID) : false));
		}
	}

	private static boolean isPowered(IGrid grid)
	{
		if (grid == null)
		{
			return false;
		}
		else
		{
			IEnergyGrid eg = (IEnergyGrid) grid.getCache(IEnergyGrid.class);
			return eg.isNetworkPowered();
		}
	}

	private static boolean checkPlayerPermissions(IGrid grid, int playerID)
	{
		if (grid == null)
		{
			return false;
		}
		else
		{
			ISecurityGrid gs = (ISecurityGrid) grid.getCache(ISecurityGrid.class);
			return gs == null ? false : (!gs.isAvailable() ? false : !gs.hasPermission(playerID, SecurityPermissions.BUILD));
		}
	}

	public static boolean isDrawing(Tessellator tess)
	{
		return false;
	}

	public static void configurePlayer(EntityPlayer player, ForgeDirection side, TileEntity tile)
	{
		float pitch = 0.0F;
		float yaw = 0.0F;
		player.yOffset = 1.8F;
		switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[side.ordinal()])
		{
			case 1:
				yaw = 180.0F;
				break;
			case 2:
				yaw = 0.0F;
				break;
			case 3:
				yaw = -90.0F;
				break;
			case 4:
				yaw = 90.0F;
				break;
			case 5:
				pitch = 90.0F;
				break;
			case 6:
				pitch = 90.0F;
				player.yOffset = -1.8F;
			case 7:
		}

		player.posX = (double) ((float) tile.xCoord) + 0.5D;
		player.posY = (double) ((float) tile.yCoord) + 0.5D;
		player.posZ = (double) ((float) tile.zCoord) + 0.5D;
		player.rotationPitch = player.prevCameraPitch = player.cameraPitch = pitch;
		player.rotationYaw = player.prevCameraYaw = player.cameraYaw = yaw;
	}

	public static boolean canAccess(AENetworkProxy gridProxy, BaseActionSource src)
	{
		try
		{
			if (src.isPlayer())
			{
				return gridProxy.getSecurity().hasPermission(((PlayerSource) src).player, SecurityPermissions.BUILD);
			}
			else if (src.isMachine())
			{
				IActionHost gae = ((MachineSource) src).via;
				IGridNode n = gae.getActionableNode();
				if (n == null)
				{
					return false;
				}
				else
				{
					int playerID = n.getPlayerID();
					return gridProxy.getSecurity().hasPermission(playerID, SecurityPermissions.BUILD);
				}
			}
			else
			{
				return false;
			}
		}
		catch (GridAccessException var5)
		{
			return false;
		}
	}

	public static ItemStack extractItemsByRecipe(IEnergySource energySrc, BaseActionSource mySrc, IMEMonitor<IAEItemStack> src, World w, IRecipe r, ItemStack output, InventoryCrafting ci, ItemStack providedTemplate, int slot, IItemList<IAEItemStack> aitems, Actionable realForFake, IPartitionList<IAEItemStack> filter)
	{
		if (energySrc.extractAEPower(1.0D, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9D)
		{
			if (providedTemplate == null)
			{
				return null;
			}

			AEItemStack ae_req = AEItemStack.create(providedTemplate);
			ae_req.setStackSize(1L);
			if (filter == null || filter.isListed(ae_req))
			{
				IAEItemStack checkFuzzy = (IAEItemStack) src.extractItems(ae_req, realForFake, mySrc);
				if (checkFuzzy != null)
				{
					ItemStack i$ = checkFuzzy.getItemStack();
					if (i$ != null)
					{
						energySrc.extractAEPower(1.0D, realForFake, PowerMultiplier.CONFIG);
						return i$;
					}
				}
			}

			boolean checkFuzzy1 = ae_req.isOre() || providedTemplate.getItemDamage() == 32767 || providedTemplate.hasTagCompound() || providedTemplate.isItemStackDamageable();
			if (aitems != null && checkFuzzy1)
			{
				Iterator i$1 = aitems.iterator();

				while (i$1.hasNext())
				{
					IAEItemStack x = (IAEItemStack) i$1.next();
					ItemStack sh = x.getItemStack();
					if ((isSameItemType(providedTemplate, sh) || ae_req.sameOre(x)) && !isSameItem(sh, output))
					{
						ItemStack cp = cloneItemStack(sh);
						cp.stackSize = 1;
						ci.setInventorySlotContents(slot, cp);
						if (r.matches(ci, w) && isSameItem(r.getCraftingResult(ci), output))
						{
							IAEItemStack ax = x.copy();
							ax.setStackSize(1L);
							if (filter == null || filter.isListed(ax))
							{
								IAEItemStack ex = (IAEItemStack) src.extractItems(ax, realForFake, mySrc);
								if (ex != null)
								{
									energySrc.extractAEPower(1.0D, realForFake, PowerMultiplier.CONFIG);
									return ex.getItemStack();
								}
							}
						}

						ci.setInventorySlotContents(slot, providedTemplate);
					}
				}
			}
		}

		return null;
	}

	public static ItemStack getContainerItem(ItemStack stackInSlot)
	{
		if (stackInSlot == null)
		{
			return null;
		}
		else
		{
			Item i = stackInSlot.getItem();
			if (i != null && i.hasContainerItem(stackInSlot))
			{
				ItemStack ci = i.getContainerItem(stackInSlot.copy());
				if (ci != null && ci.isItemStackDamageable() && ci.getItemDamage() == ci.getMaxDamage())
				{
					ci = null;
				}

				return ci;
			}
			else if (stackInSlot.stackSize > 1)
			{
				--stackInSlot.stackSize;
				return stackInSlot;
			}
			else
			{
				return null;
			}
		}
	}

	public static void notifyBlocksOfNeighbors(World worldObj, int xCoord, int yCoord, int zCoord)
	{
		if (!worldObj.isRemote)
		{
			TickHandler.instance.addCallable(worldObj, new BlockUpdate(worldObj, xCoord, yCoord, zCoord));
		}

	}

	public static boolean canRepair(AEFeature type, ItemStack a, ItemStack b)
	{
		return b != null && a != null ? (type == AEFeature.CertusQuartzTools ? AEApi.instance().materials().materialCertusQuartzCrystal.sameAsStack(b) : (type == AEFeature.NetherQuartzTools ? Items.quartz == b.getItem() : false)) : false;
	}

	public static Object findPrefered(ItemStack[] is)
	{
		ItemStack[] arr$ = is;
		int len$ = is.length;

		for (int i$ = 0; i$ < len$; ++i$)
		{
			ItemStack stack = arr$[i$];
			if (AEApi.instance().parts().partCableGlass.sameAs(AEColor.Transparent, stack))
			{
				return stack;
			}

			if (AEApi.instance().parts().partCableCovered.sameAs(AEColor.Transparent, stack))
			{
				return stack;
			}

			if (AEApi.instance().parts().partCableSmart.sameAs(AEColor.Transparent, stack))
			{
				return stack;
			}

			if (AEApi.instance().parts().partCableDense.sameAs(AEColor.Transparent, stack))
			{
				return stack;
			}
		}

		return is;
	}

	public static void sendChunk(Chunk c, int verticalBits)
	{
		try
		{
			WorldServer t = (WorldServer) c.worldObj;
			PlayerManager pm = t.getPlayerManager();
			if (getOrCreateChunkWatcher == null)
			{
				getOrCreateChunkWatcher = ReflectionHelper.findMethod(PlayerManager.class, pm, new String[] { "getOrCreateChunkWatcher", "func_72690_a" }, new Class[] { Integer.TYPE, Integer.TYPE, Boolean.TYPE });
			}

			if (getOrCreateChunkWatcher != null)
			{
				Object playerinstance = getOrCreateChunkWatcher.invoke(pm, new Object[] { Integer.valueOf(c.xPosition), Integer.valueOf(c.zPosition), Boolean.valueOf(false) });
				if (playerinstance != null)
				{
					Playerinstance = playerinstance.getClass();
					if (sendToAllPlayersWatchingChunk == null)
					{
						sendToAllPlayersWatchingChunk = ReflectionHelper.findMethod(Playerinstance, playerinstance, new String[] { "sendToAllPlayersWatchingChunk", "func_151251_a" }, new Class[] { Packet.class });
					}

					if (sendToAllPlayersWatchingChunk != null)
					{
						sendToAllPlayersWatchingChunk.invoke(playerinstance, new Object[] { new S21PacketChunkData(c, false, verticalBits) });
					}
				}
			}
		}
		catch (Throwable var5)
		{
			AELog.error(var5);
		}

	}

	public static AxisAlignedBB getPrimaryBox(ForgeDirection side, int facadeThickness)
	{
		switch (SyntheticClass_1.$SwitchMap$net$minecraftforge$common$util$ForgeDirection[side.ordinal()])
		{
			case 1:
				return AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, (double) facadeThickness / 16.0D);
			case 2:
				return AxisAlignedBB.getBoundingBox(0.0D, 0.0D, (16.0D - (double) facadeThickness) / 16.0D, 1.0D, 1.0D, 1.0D);
			case 3:
				return AxisAlignedBB.getBoundingBox((16.0D - (double) facadeThickness) / 16.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
			case 4:
				return AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, (double) facadeThickness / 16.0D, 1.0D, 1.0D);
			case 5:
				return AxisAlignedBB.getBoundingBox(0.0D, (16.0D - (double) facadeThickness) / 16.0D, 0.0D, 1.0D, 1.0D, 1.0D);
			case 6:
				return AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 1.0D, (double) facadeThickness / 16.0D, 1.0D);
			default:
				return AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
		}
	}

	public static float getEyeOffset(EntityPlayer player)
	{
		assert player.worldObj.isRemote : "Valid only on client";

		return (float) (player.posY + (double) player.getEyeHeight() - (double) player.getDefaultEyeHeight());
	}

	public static boolean isRecipePrioritized(ItemStack what)
	{
		return AEApi.instance().materials().materialPureifiedCertusQuartzCrystal.sameAsStack(what) || AEApi.instance().materials().materialPureifiedFluixCrystal.sameAsStack(what) || AEApi.instance().materials().materialPureifiedNetherQuartzCrystal.sameAsStack(what);
	}

	// $FF: synthetic class
	static class SyntheticClass_1
	{
		// $FF: synthetic field
		static final int[] $SwitchMap$net$minecraftforge$common$util$ForgeDirection = new int[ForgeDirection.values().length];

		static
		{
			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.NORTH.ordinal()] = 1;
			}
			catch (NoSuchFieldError var7)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.SOUTH.ordinal()] = 2;
			}
			catch (NoSuchFieldError var6)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.EAST.ordinal()] = 3;
			}
			catch (NoSuchFieldError var5)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.WEST.ordinal()] = 4;
			}
			catch (NoSuchFieldError var4)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.UP.ordinal()] = 5;
			}
			catch (NoSuchFieldError var3)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.DOWN.ordinal()] = 6;
			}
			catch (NoSuchFieldError var2)
			{
				;
			}

			try
			{
				$SwitchMap$net$minecraftforge$common$util$ForgeDirection[ForgeDirection.UNKNOWN.ordinal()] = 7;
			}
			catch (NoSuchFieldError var1)
			{
				;
			}
		}
	}

	// TODO gamerforEA code start
	/**
	 * 
	 * @param x - X coord
	 * @param y - Y coord
	 * @param z - Z coord
	 * @param w - World
	 * @param p - EntityPlayer
	 * @return true if canceled
	 */
	public static boolean callBlockBreakEvent(int x, int y, int z, World w, EntityPlayer p)
	{
		BreakEvent event = new BreakEvent(x, y, z, w, w.getBlock(x, y, z), w.getBlockMetadata(x, y, z), p);
		return MinecraftForge.EVENT_BUS.post(event);
	}
	// TODO gamerforEA code end
}
