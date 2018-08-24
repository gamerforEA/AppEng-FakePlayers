package com.gamerforea.ae.coremod;

import codechicken.multipart.BlockMultipart;
import com.gamerforea.ae.ModUtils;
import net.minecraft.world.World;

public final class MethodHooks
{
	public static final String OWNER = "com/gamerforea/ae/coremod/MethodHooks";
	public static final String NAME = "dropAndDestroy";
	public static final String DESC = "(Lcodechicken/multipart/BlockMultipart;Lnet/minecraft/world/World;III)V";

	public static void dropAndDestroy(BlockMultipart block, World world, int x, int y, int z)
	{
		Boolean prevBreaking = ModUtils.IS_BLOCK_BREAKING.get();
		ModUtils.IS_BLOCK_BREAKING.set(Boolean.TRUE);
		try
		{
			block.dropAndDestroy(world, x, y, z);
		}
		finally
		{
			ModUtils.IS_BLOCK_BREAKING.set(prevBreaking);
		}
	}
}
