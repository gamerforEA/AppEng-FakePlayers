package com.gamerforea.ae;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.gamerforea.eventhelper.nexus.ModNexus;
import com.gamerforea.eventhelper.nexus.ModNexusFactory;
import com.gamerforea.eventhelper.nexus.NexusUtils;
import com.google.common.base.Objects;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.lang.reflect.Method;
import java.util.Collection;

@ModNexus(name = "AppEng", uuid = "2c6e6150-dcf8-4e8c-a6e0-7ea1935eb0cf")
public final class ModUtils
{
	public static final ThreadLocal<Boolean> IS_BLOCK_BREAKING = new ThreadLocal<>();
	public static final ModNexusFactory NEXUS_FACTORY = NexusUtils.getFactory();

	public static FakePlayer getModFake(final World world)
	{
		return NEXUS_FACTORY.getFake(world);
	}

	public static TileEntity safeCastToTileEntity(Object object)
	{
		return object instanceof TileEntity ? (TileEntity) object : null;
	}

	public static boolean isValidTile(TileEntity tile)
	{
		return tile == null || tile.hasWorldObj() && tile.getWorldObj().blockExists(tile.xCoord, tile.yCoord, tile.zCoord) && tile.getWorldObj().getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile;
	}

	public static boolean needClearInvOnBreak()
	{
		return EventConfig.clearInvOnBreak && Objects.firstNonNull(ModUtils.IS_BLOCK_BREAKING.get(), Boolean.FALSE);
	}

	public static void getInventoryContent(IInventory inventory, Collection<ItemStack> stacks, boolean clearInventory, boolean forceCopyStacks)
	{
		if (inventory != null)
			for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
			{
				ItemStack stack = inventory.getStackInSlot(slot);
				if (stack != null && stack.stackSize > 0 && stack.getItem() != null)
				{
					if (clearInventory)
					{
						stacks.add(stack.copy());
						inventory.setInventorySlotContents(slot, null);
					}
					else
						stacks.add(forceCopyStacks ? stack.copy() : stack);
				}
			}
	}

	public static <T extends IStackWatcherHost> boolean canSendDirtyFlagForStackWatcherHost(T instance, Class<? extends T> expectedClass)
	{
		return isMethodDeclaredInClass(instance, expectedClass, "onStackChange", IItemList.class, IAEStack.class, IAEStack.class, BaseActionSource.class, StorageChannel.class);
	}

	public static <T> boolean isMethodDeclaredInClass(T instance, Class<? extends T> expectedClass, String methodName, Class<?>... parameterTypes)
	{
		if (instance == null)
			return false;
		Class<?> instanceClass = instance.getClass();
		Method method;
		try
		{
			method = instanceClass.getMethod(methodName, parameterTypes);
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
			return false;
		}
		return method.getDeclaringClass() == expectedClass;
	}
}
