package com.gamerforea.ae;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.gamerforea.wgew.cauldron.event.CauldronBlockBreakEvent;
import com.gamerforea.wgew.cauldron.event.CauldronEntityDamageByEntityEvent;

public final class FakePlayerUtils
{
	public static BlockBreakEvent callBlockBreakEvent(int x, int y, int z, EntityPlayer player)
	{
		CauldronBlockBreakEvent event = new CauldronBlockBreakEvent(player, x, y, z);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getBukkitEvent();
	}

	public static EntityDamageByEntityEvent callEntityDamageByEntityEvent(Entity damager, Entity damagee, DamageCause cause, double damage)
	{
		CauldronEntityDamageByEntityEvent event = new CauldronEntityDamageByEntityEvent(damager, damagee, cause, damage);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return event.getBukkitEvent();
	}
}