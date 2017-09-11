package com.gamerforea.ae;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class BusUtils
{
	private static final Cache<TileEntity, Boolean> notifyCache = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(5, TimeUnit.SECONDS).build();

	public static final boolean checkBusCanInteract(TileEntity busTile, TileEntity targetTile)
	{
		return checkBusCanInteract(busTile, targetTile, false);
	}

	public static final boolean checkBusCanInteract(TileEntity busTile, TileEntity targetTile, boolean isStorage)
	{
		if (busTile != null && targetTile != null)
		{
			Block targetBlock = targetTile.getBlockType();
			int targetMeta = targetTile.getBlockMetadata();
			if (EventConfig.inList(EventConfig.busBlackList, targetBlock, targetMeta))
				return false;

			if (!isValidTile(busTile) || !isValidTile(targetTile))
				return false;

			if (EventConfig.busSameChunk && (!EventConfig.busSameChunkStorageOnly || isStorage) && !isSameChunk(busTile, targetTile))
			{
				if (EventConfig.busSameChunkMessage && notifyCache.getIfPresent(busTile) == null)
				{
					sendMessage(busTile);
					notifyCache.put(busTile, true);
				}
				return false;
			}
		}

		return true;
	}

	public static final boolean isValidTile(TileEntity t1)
	{
		if (t1 != null)
		{
			World world = t1.getWorldObj();
			if (world != null && world.blockExists(t1.xCoord, t1.yCoord, t1.zCoord) && world.getTileEntity(t1.xCoord, t1.yCoord, t1.zCoord) == t1)
			{
				if (t1 instanceof TileEntityChest)
				{
					TileEntity t2 = getSecondChest((TileEntityChest) t1);
					if (t2 != null)
						return world.blockExists(t2.xCoord, t2.yCoord, t2.zCoord) && world.getTileEntity(t2.xCoord, t2.yCoord, t2.zCoord) == t2;
				}
				return true;
			}
		}
		return false;
	}

	// Copy from Platform.GetChestInv(TileEntity):IInventory
	private static TileEntity getSecondChest(final TileEntityChest te)
	{
		TileEntityChest teA = (TileEntityChest) te;
		TileEntity teB = null;
		final Block myBlockID = teA.getWorldObj().getBlock(teA.xCoord, teA.yCoord, teA.zCoord);

		if (teA.getWorldObj().getBlock(teA.xCoord + 1, teA.yCoord, teA.zCoord) == myBlockID)
		{
			teB = teA.getWorldObj().getTileEntity(teA.xCoord + 1, teA.yCoord, teA.zCoord);
			if (!(teB instanceof TileEntityChest))
			{
				teB = null;
			}
		}

		if (teB == null)
		{
			if (teA.getWorldObj().getBlock(teA.xCoord - 1, teA.yCoord, teA.zCoord) == myBlockID)
			{
				teB = teA.getWorldObj().getTileEntity(teA.xCoord - 1, teA.yCoord, teA.zCoord);
				if (!(teB instanceof TileEntityChest))
				{
					teB = null;
				}
				else
				{
					final TileEntityChest x = teA;
					teA = (TileEntityChest) teB;
					teB = x;
				}
			}
		}

		if (teB == null)
		{
			if (teA.getWorldObj().getBlock(teA.xCoord, teA.yCoord, teA.zCoord + 1) == myBlockID)
			{
				teB = teA.getWorldObj().getTileEntity(teA.xCoord, teA.yCoord, teA.zCoord + 1);
				if (!(teB instanceof TileEntityChest))
				{
					teB = null;
				}
			}
		}

		if (teB == null)
		{
			if (teA.getWorldObj().getBlock(teA.xCoord, teA.yCoord, teA.zCoord - 1) == myBlockID)
			{
				teB = teA.getWorldObj().getTileEntity(teA.xCoord, teA.yCoord, teA.zCoord - 1);
				if (!(teB instanceof TileEntityChest))
				{
					teB = null;
				}
				else
				{
					final TileEntityChest x = teA;
					teA = (TileEntityChest) teB;
					teB = x;
				}
			}
		}

		return teB == te ? teA : teB;
	}

	private static final boolean isSameChunk(TileEntity t1, TileEntity t2)
	{
		if (t1 != null && t2 != null)
		{
			int chunkX1 = t1.xCoord >> 4;
			int chunkX2 = t2.xCoord >> 4;
			int chunkZ1 = t1.zCoord >> 4;
			int chunkZ2 = t2.zCoord >> 4;
			return chunkX1 == chunkX2 && chunkZ1 == chunkZ2;
		}

		return false;
	}

	private static final void sendMessage(TileEntity tile)
	{
		if (tile != null)
		{
			int x = tile.xCoord;
			int y = tile.yCoord;
			int z = tile.zCoord;
			int r = 6;
			AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x - r, y - r, z - r, x + r, y + r, z + r);
			List<EntityPlayer> list = tile.getWorldObj().getEntitiesWithinAABB(EntityPlayer.class, aabb);
			for (EntityPlayer player : list)
			{
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Шины могут взаимодействовать с блоками только в своём чанке!"));
			}
		}
	}
}
