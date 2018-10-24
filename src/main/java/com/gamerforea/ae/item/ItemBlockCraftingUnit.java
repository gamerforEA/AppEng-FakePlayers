package com.gamerforea.ae.item;

import appeng.block.AEBaseItemBlock;
import appeng.block.crafting.BlockCraftingUnit;
import com.gamerforea.ae.EventConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemBlockCraftingUnit extends AEBaseItemBlock
{
	private static final ForgeDirection[] CHECKED_DIRECTIONS = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST };

	public ItemBlockCraftingUnit(Block id)
	{
		super(id);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		if (EventConfig.denySplitCraftingUnitsByChunks)
		{
			int chunkX = x >> 4;
			int chunkZ = z >> 4;
			for (ForgeDirection direction : CHECKED_DIRECTIONS)
			{
				int xx = x + direction.offsetX;
				int zz = z + direction.offsetZ;
				int chunkXX = xx >> 4;
				int chunkZZ = zz >> 4;
				if (chunkXX != chunkX || chunkZZ != chunkZ)
				{
					Block neighborBlock = w.getBlock(xx, y, zz);
					if (neighborBlock instanceof BlockCraftingUnit)
						return false;
				}
			}
		}
		return super.placeBlockAt(stack, player, w, x, y, z, side, hitX, hitY, hitZ, metadata);
	}
}
