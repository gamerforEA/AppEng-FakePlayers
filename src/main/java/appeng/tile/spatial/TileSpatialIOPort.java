/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.tile.spatial;

import java.util.UUID;
import java.util.concurrent.Callable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.YesNo;
import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkSpatialEvent;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.hooks.TickHandler;
import appeng.items.storage.ItemSpatialStorageCell;
import appeng.me.cache.SpatialPylonCache;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

import com.gamerforea.ae.FakePlayerUtils;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;

public class TileSpatialIOPort extends AENetworkInvTile implements Callable
{
	final int[] sides = { 0, 1 };
	final AppEngInternalInventory inv = new AppEngInternalInventory(this, 2);
	YesNo lastRedstoneState = YesNo.UNDECIDED;

	// TODO gamerforEA code start
	public GameProfile ownerProfile;
	private FakePlayer ownerFake;

	public FakePlayer getOwnerFake()
	{
		if (this.ownerFake != null) return this.ownerFake;
		else if (this.ownerProfile != null) return this.ownerFake = FakePlayerUtils.create(this.worldObj, this.ownerProfile);
		else return FakePlayerUtils.getModFake(this.worldObj);
	}
	// TODO gamerforEA code end

	public TileSpatialIOPort()
	{
		this.gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileSpatialIOPort(NBTTagCompound data)
	{
		data.setInteger("lastRedstoneState", this.lastRedstoneState.ordinal());
		// TODO gamerforEA code start
		if (this.ownerProfile != null)
		{
			data.setString("ownerUUID", this.ownerProfile.getId().toString());
			data.setString("ownerName", this.ownerProfile.getName());
		}
		// TODO gamerforEA code end
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileSpatialIOPort(NBTTagCompound data)
	{
		if (data.hasKey("lastRedstoneState"))
		{
			this.lastRedstoneState = YesNo.values()[data.getInteger("lastRedstoneState")];
		}
		// TODO gamerforEA code start
		String uuid = data.getString("ownerUUID");
		if (!Strings.isNullOrEmpty(uuid))
		{
			String name = data.getString("ownerName");
			if (!Strings.isNullOrEmpty(name)) this.ownerProfile = new GameProfile(UUID.fromString(uuid), name);
		}
		// TODO gamerforEA code end
	}

	public boolean getRedstoneState()
	{
		if (this.lastRedstoneState == YesNo.UNDECIDED)
		{
			this.updateRedstoneState();
		}

		return this.lastRedstoneState == YesNo.YES;
	}

	public void updateRedstoneState()
	{
		YesNo currentState = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord) ? YesNo.YES : YesNo.NO;
		if (this.lastRedstoneState != currentState)
		{
			this.lastRedstoneState = currentState;
			if (this.lastRedstoneState == YesNo.YES)
			{
				this.triggerTransition();
			}
		}
	}

	private void triggerTransition()
	{
		if (Platform.isServer())
		{
			ItemStack cell = this.getStackInSlot(0);
			if (this.isSpatialCell(cell))
			{
				TickHandler.INSTANCE.addCallable(null, this);// this needs to be cross world synced.
			}
		}
	}

	private boolean isSpatialCell(ItemStack cell)
	{
		if (cell != null && cell.getItem() instanceof ISpatialStorageCell)
		{
			ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();
			return sc != null && sc.isSpatialStorage(cell);
		}
		return false;
	}

	@Override
	public Object call() throws Exception
	{
		ItemStack cell = this.getStackInSlot(0);
		if (this.isSpatialCell(cell) && this.getStackInSlot(1) == null)
		{
			IGrid gi = this.gridProxy.getGrid();
			IEnergyGrid energy = this.gridProxy.getEnergy();

			ISpatialStorageCell sc = (ISpatialStorageCell) cell.getItem();

			SpatialPylonCache spc = gi.getCache(ISpatialCache.class);
			if (spc.hasRegion() && spc.isValidRegion())
			{
				double req = spc.requiredPower();
				double pr = energy.extractAEPower(req, Actionable.SIMULATE, PowerMultiplier.CONFIG);
				if (Math.abs(pr - req) < req * 0.001)
				{
					MENetworkEvent res = gi.postEvent(new MENetworkSpatialEvent(this, req));
					if (!res.isCanceled())
					{
						// TODO gamerforEA code replace, old code: TransitionResult tr = sc.doSpatialTransition(cell, this.worldObj, spc.getMin(), spc.getMax(), true);
						TransitionResult tr = null;
						if (sc instanceof ItemSpatialStorageCell) tr = ((ItemSpatialStorageCell) sc).doSpatialTransition(this.getOwnerFake(), cell, this.worldObj, spc.getMin(), spc.getMax(), true);
						else tr = sc.doSpatialTransition(cell, this.worldObj, spc.getMin(), spc.getMax(), true);
						// TODO gamerforEA code end
						if (tr.success)
						{
							energy.extractAEPower(req, Actionable.MODULATE, PowerMultiplier.CONFIG);
							this.setInventorySlotContents(0, null);
							this.setInventorySlotContents(1, cell);
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord(this);
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return (i == 0 && this.isSpatialCell(itemstack));
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
	}

	@Override
	public boolean canInsertItem(int slotIndex, ItemStack insertingItem, int side)
	{
		return this.isItemValidForSlot(slotIndex, insertingItem);
	}

	@Override
	public boolean canExtractItem(int slotIndex, ItemStack extractedItem, int side)
	{
		return slotIndex == 1;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		return this.sides;
	}
}