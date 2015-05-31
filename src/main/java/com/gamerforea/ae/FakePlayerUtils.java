package com.gamerforea.ae;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gamerforea.wgew.cauldron.event.CauldronBlockBreakEvent;
import com.gamerforea.wgew.cauldron.event.CauldronEntityDamageByEntityEvent;

public final class FakePlayerUtils
{
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