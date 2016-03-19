package com.gamerforea.ae;

import com.gamerforea.eventhelper.fake.FakePlayerContainer;
import com.mojang.authlib.GameProfile;

import appeng.api.implementations.IUpgradeableHost;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public final class FakePlayerContainerUpgradeableHost extends FakePlayerContainer
{
	private final IUpgradeableHost host;

	public FakePlayerContainerUpgradeableHost(FakePlayer modFake, IUpgradeableHost host)
	{
		super(modFake);
		this.host = host;
	}

	public FakePlayerContainerUpgradeableHost(GameProfile modFakeProfile, IUpgradeableHost host)
	{
		super(modFakeProfile);
		this.host = host;
	}

	@Override
	public World getWorld()
	{
		return this.host.getTile().getWorldObj();
	}
}