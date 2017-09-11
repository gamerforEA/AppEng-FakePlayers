package com.gamerforea.ae;

import com.gamerforea.eventhelper.EventHelper;
import com.gamerforea.eventhelper.util.ConvertUtils;
import com.gamerforea.eventhelper.util.FastUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public final class ModUtils
{
	public static final GameProfile profile = new GameProfile(UUID.fromString("2c6e6150-dcf8-4e8c-a6e0-7ea1935eb0cf"), "[AppEng]");
	private static FakePlayer player = null;

	public static final FakePlayer getModFake(final World world)
	{
		if (player == null)
			player = FastUtils.getFake(world, profile);
		else
			player.worldObj = world;

		return player;
	}

	public static final boolean hasPermission(EntityPlayer player, String permisssion)
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
}
