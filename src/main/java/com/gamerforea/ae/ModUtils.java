package com.gamerforea.ae;

import com.gamerforea.eventhelper.EventHelper;
import com.gamerforea.eventhelper.util.ConvertUtils;
import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Collection;
import java.util.UUID;

public final class ModUtils
{
	public static final ThreadLocal<Boolean> IS_BLOCK_BREAKING = new ThreadLocal<>();
	public static final GameProfile profile = new GameProfile(UUID.fromString("2c6e6150-dcf8-4e8c-a6e0-7ea1935eb0cf"), "[AppEng]");
	private static FakePlayer player = null;

	public static FakePlayer getModFake(final World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}

	public static boolean hasPermission(EntityPlayer player, String permisssion)
	{
		try
		{
			return ConvertUtils.toBukkitEntity(player).hasPermission(permisssion);
		}
		catch (Exception e)
		{
			if (EventHelper.debug)
				e.printStackTrace();
			return false;
		}
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
}
