package appeng.items.tools.quartz;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.transformer.annotations.integration.Interface;
import appeng.util.Platform;
import buildcraft.api.tools.IToolWrench;

@Interface(iface = "buildcraft.api.tools.IToolWrench", iname = "BC")
public class ToolQuartzWrench extends AEBaseItem implements IAEWrench, IToolWrench
{
	public ToolQuartzWrench(AEFeature type)
	{
		super(ToolQuartzWrench.class, type.name());
		setfeature(EnumSet.of(type, AEFeature.QuartzWrench));
		setMaxStackSize(1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		Block b = world.getBlock(x, y, z);
		if (b != null && !player.isSneaking() && Platform.hasPermissions(new DimensionalCoord(world, x, y, z), player))
		{
			if (Platform.isClient()) return !world.isRemote;

			// TODO gamerforEA code start
			if (callBlockBreakEvent(x, y, z, world, player)) return false;
			// TODO gamerforEA code end

			ForgeDirection mySide = ForgeDirection.getOrientation(side);
			if (b.rotateBlock(world, x, y, z, mySide))
			{
				b.onNeighborBlockChange(world, x, y, z, Platform.air);
				player.swingItem();
				return !world.isRemote;
			}
		}
		return false;
	}

	@Override
	// public boolean shouldPassSneakingClickToBlock(World w, int x, int y, int z)
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean canWrench(ItemStack is, EntityPlayer player, int x, int y, int z)
	{
		return !callBlockBreakEvent(x, y, z, player.worldObj, player); // TODO gamerforEA call event
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z)
	{
		return !callBlockBreakEvent(x, y, z, player.worldObj, player); // TODO gamerforEA call event
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z)
	{
		player.swingItem();
	}

	// TODO gamerforEA code start
	/**
	 * 
	 * @param x - X coord
	 * @param y - Y coord
	 * @param z - Z coord
	 * @param w - World
	 * @param p - EntityPlayer
	 * @return true if canceled
	 */
	public static boolean callBlockBreakEvent(int x, int y, int z, World w, EntityPlayer p)
	{
		BreakEvent event = new BreakEvent(x, y, z, w, w.getBlock(x, y, z), w.getBlockMetadata(x, y, z), p);
		return MinecraftForge.EVENT_BUS.post(event);
	}
	// TODO gamerforEA code end
}