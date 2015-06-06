package com.gamerforea.ae;

import java.lang.ref.WeakReference;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gamerforea.wgew.cauldron.event.CauldronBlockBreakEvent;
import com.gamerforea.wgew.cauldron.event.CauldronEntityDamageByEntityEvent;
import com.mojang.authlib.GameProfile;

public final class FakePlayerUtils
{
	private static WeakReference<FakePlayer> player = new WeakReference<FakePlayer>(null);

	public static final FakePlayer getModFake(World world)
	{
		if (player.get() == null)
		{
			GameProfile profile = new GameProfile(UUID.fromString("2c6e6150-dcf8-4e8c-a6e0-7ea1935eb0cf"), "[AppEng]");
			player = new WeakReference<FakePlayer>(create(world, profile));
		}
		else player.get().worldObj = world;

		return player.get();
	}

	public static FakePlayer create(World world, GameProfile profile)
	{
		return FakePlayerFactory.get((WorldServer) world, profile);
	}

	public static boolean cantBreak(EntityPlayer player, int x, int y, int z)
	{
		CauldronBlockBreakEvent event = new CauldronBlockBreakEvent(player, x, y, z);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getBukkitEvent().isCancelled();
	}

	public static boolean cantDamage(Entity damager, Entity damagee)
	{
		CauldronEntityDamageByEntityEvent event = new CauldronEntityDamageByEntityEvent(damager, damagee, DamageCause.ENTITY_ATTACK, 0D);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getBukkitEvent().isCancelled();
	}
}