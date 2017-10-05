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
	private static final TileEntity[] EMPTY_ARRAY = new TileEntity[0];
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
				for (TileEntity t2 : getSecondTiles(t1))
				{
					if (t2 != null && (!world.blockExists(t2.xCoord, t2.yCoord, t2.zCoord) || world.getTileEntity(t2.xCoord, t2.yCoord, t2.zCoord) != t2))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	public static final TileEntity[] getSecondTiles(final TileEntity te)
	{
		if (!(te instanceof TileEntityChest))
			return EMPTY_ARRAY;

		TileEntityChest chest = (TileEntityChest) te;
		return new TileEntity[] { chest.adjacentChestXNeg, chest.adjacentChestXPos, chest.adjacentChestZNeg, chest.adjacentChestZPos };
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
