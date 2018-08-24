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

package appeng.container.implementations;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class ContainerNetworkTool extends AEBaseContainer
{
	private final INetworkTool toolInv;

	// TODO gamerforEA code start
	private static final String NBT_KEY_UID = "UID";
	private final ItemStack stack;
	private final int stackSlot;

	@Override
	public boolean isValidContainer()
	{
		return super.isValidContainer() && isSameItemInventory(this.stack, this.getInventoryPlayer().getCurrentItem());
	}

	@Override
	public ItemStack slotClick(int slot, int button, int buttonType, EntityPlayer player)
	{
		if (slot == this.stackSlot)
			return null;
		if (buttonType == 2 && button == this.stackSlot)
			return null;
		if (!this.isValidContainer())
			return null;
		return super.slotClick(slot, button, buttonType, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		return slot == this.stackSlot || !this.isValidContainer() ? null : super.transferStackInSlot(player, slot);
	}

	private static boolean isSameItemInventory(ItemStack base, ItemStack comparison)
	{
		if (base == null || comparison == null)
			return false;

		if (base.getItem() != comparison.getItem())
			return false;

		if (!base.hasTagCompound() || !comparison.hasTagCompound())
			return false;

		String baseUID = base.getTagCompound().getString(NBT_KEY_UID);
		String comparisonUID = comparison.getTagCompound().getString(NBT_KEY_UID);
		return baseUID != null && baseUID.equals(comparisonUID);
	}
	// TODO gamerforEA code end

	@GuiSync(1)
	public boolean facadeMode;

	public ContainerNetworkTool(final InventoryPlayer ip, final INetworkTool te)
	{
		super(ip, null, null);
		this.toolInv = te;

		int currentItem = ip.currentItem;
		this.lockPlayerInventorySlot(currentItem);

		// TODO gamerforEA code start
		this.stack = ip.getStackInSlot(currentItem);
		if (this.stack != null && this.stack.getItem() instanceof INetworkTool)
		{
			if (!this.stack.hasTagCompound())
				this.stack.setTagCompound(new NBTTagCompound());
			NBTTagCompound nbt = this.stack.getTagCompound();
			if (!nbt.hasKey(NBT_KEY_UID))
				nbt.setString(NBT_KEY_UID, UUID.randomUUID().toString());
		}
		// TODO gamerforEA code end

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				this.addSlotToContainer(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, te, y * 3 + x, 80 - 18 + x * 18, 37 - 18 + y * 18, this.getInventoryPlayer()));
			}
		}

		this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);

		// TODO gamerforEA code start
		int stackSlot = -1;
		for (Slot slot : (Iterable<? extends Slot>) this.inventorySlots)
		{
			if (slot.getSlotIndex() == currentItem)
			{
				stackSlot = slot.slotNumber;
				break;
			}
		}
		this.stackSlot = stackSlot;
		// TODO gamerforEA code end
	}

	public void toggleFacadeMode()
	{
		final NBTTagCompound data = Platform.openNbtData(this.toolInv.getItemStack());
		data.setBoolean("hideFacades", !data.getBoolean("hideFacades"));
		this.detectAndSendChanges();
	}

	@Override
	public void detectAndSendChanges()
	{
		final ItemStack currentItem = this.getPlayerInv().getCurrentItem();

		if (currentItem != this.toolInv.getItemStack())
			if (currentItem != null)
				if (Platform.isSameItem(this.toolInv.getItemStack(), currentItem))
					this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.toolInv.getItemStack());
				else
					this.setValidContainer(false);
			else
				this.setValidContainer(false);

		if (this.isValidContainer())
		{
			final NBTTagCompound data = Platform.openNbtData(currentItem);
			this.setFacadeMode(data.getBoolean("hideFacades"));
		}

		super.detectAndSendChanges();
	}

	public boolean isFacadeMode()
	{
		return this.facadeMode;
	}

	private void setFacadeMode(final boolean facadeMode)
	{
		this.facadeMode = facadeMode;
	}
}
