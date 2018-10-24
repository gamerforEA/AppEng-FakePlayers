package com.gamerforea.ae;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class CooldownManager
{
	private final TObjectLongMap<UUID> cooldowns = new TObjectLongHashMap<UUID>();
	private final long cooldown;

	public CooldownManager(long cooldown, TimeUnit timeUnit)
	{
		this(cooldown <= 0 ? 0 : timeUnit.toSeconds(cooldown) * 20);
	}

	public CooldownManager(long cooldown)
	{
		this.cooldown = Math.max(cooldown, 0);
		if (this.cooldown > 0)
			FMLCommonHandler.instance().bus().register(this);
	}

	public boolean canAdd(EntityPlayer player)
	{
		return this.cooldown <= 0 || !this.cooldowns.containsKey(player.getUniqueID());
	}

	public boolean add(EntityPlayer player)
	{
		if (this.cooldown <= 0)
			return true;
		else if (this.canAdd(player))
		{
			this.cooldowns.put(player.getUniqueID(), this.cooldown);
			return true;
		}
		return false;
	}

	public long getCooldown(EntityPlayer player)
	{
		return this.cooldown <= 0 ? 0 : this.cooldowns.get(player.getUniqueID());
	}

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		for (TObjectLongIterator<UUID> iterator = this.cooldowns.iterator(); iterator.hasNext(); )
		{
			iterator.advance();
			long timer = iterator.value() - 1;
			if (timer <= 0)
				iterator.remove();
			else
				iterator.setValue(timer);
		}
	}
}
