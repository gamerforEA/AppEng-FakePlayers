package com.gamerforea.ae;

import appeng.util.inv.AdaptorIInventory;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.tileentity.TileEntity;

public final class AdaptorInventoryLargeChest extends AdaptorIInventory
{
	private final TileEntity upperChest;
	private final TileEntity lowerChest;

	public AdaptorInventoryLargeChest(InventoryLargeChest inventory)
	{
		super(inventory);
		this.upperChest = ModUtils.safeCastToTileEntity(ReflectionHelper.getPrivateValue(InventoryLargeChest.class, inventory, "field_70477_b", "upperChest"));
		this.lowerChest = ModUtils.safeCastToTileEntity(ReflectionHelper.getPrivateValue(InventoryLargeChest.class, inventory, "field_70478_c", "lowerChest"));
	}

	@Override
	public boolean isValidInventory()
	{
		return ModUtils.isValidTile(this.upperChest) && ModUtils.isValidTile(this.lowerChest);
	}
}
