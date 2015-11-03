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

package appeng.items.tools.powered;

import java.util.EnumSet;

import com.gamerforea.eventhelper.util.EventUtils;
import com.google.common.base.Optional;

import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.sync.packets.PacketLightning;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.Platform;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public class ToolChargedStaff extends AEBasePoweredItem
{
	public ToolChargedStaff()
	{
		super(AEConfig.instance.chargedStaffBattery, Optional.<String> absent());
		this.setFeature(EnumSet.of(AEFeature.ChargedStaff, AEFeature.PoweredTools));
	}

	@Override
	public boolean hitEntity(ItemStack item, EntityLivingBase target, EntityLivingBase hitter)
	{
		if (this.getAECurrentPower(item) > 300D)
		{
			this.extractAEPower(item, 300D);

			// TODO gamerforEA code start
			if (EventUtils.cantDamage(hitter, target))
				return false;
			// TODO gamerforEA code end

			if (Platform.isServer())
				for (int x = 0; x < 2; x++)
				{
					float dx = (float) (Platform.getRandomFloat() * target.width + target.boundingBox.minX);
					float dy = (float) (Platform.getRandomFloat() * target.height + target.boundingBox.minY);
					float dz = (float) (Platform.getRandomFloat() * target.width + target.boundingBox.minZ);
					CommonHelper.proxy.sendToAllNearExcept(null, dx, dy, dz, 32D, target.worldObj, new PacketLightning(dx, dy, dz));
				}
			target.attackEntityFrom(DamageSource.magic, 6F);
			return true;
		}

		return false;
	}
}