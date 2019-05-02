package com.gamerforea.ae.subcommands;

import appeng.api.networking.IGridNode;
import appeng.api.util.DimensionalCoord;
import appeng.hooks.TickHandler;
import appeng.me.Grid;
import appeng.server.ISubCommand;
import appeng.tile.networking.TileController;
import com.gamerforea.ae.EventConfig;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.gamerforea.ae.util.ChatUtils.*;
import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.RED;

public final class GridPerfSubCommand implements ISubCommand
{
	private static final int MAX_LIST_SIZE = 50;

	@Override
	public String getHelp(MinecraftServer srv)
	{
		return "Список нагруженных МЭ-сетей";
	}

	@Override
	public void call(MinecraftServer srv, String[] args, ICommandSender sender)
	{
		if (!EventConfig.gridProfiling)
		{
			sender.addChatMessage(color(text("Профилирование МЭ-сетей выключено в конфиге"), RED));
			return;
		}

		Collection<Grid> grids = toCollection(TickHandler.INSTANCE.getGridList());
		List<Grid> topGrids = grids.stream().sorted(Comparator.comparingLong(Grid::getAverageUpdateTime).reversed()).limit(MAX_LIST_SIZE).collect(Collectors.toList());
		for (int i = 0; i < topGrids.size(); i++)
		{
			Grid topGrid = topGrids.get(i);
			long averageUpdateTime = topGrid.getAverageUpdateTime();
			sender.addChatMessage(text((i + 1) + ". ").appendSibling(color(text(averageUpdateTime + " ns"), AQUA)).appendText(" [").appendSibling(dimCoordToChatComponent(getGridCoords(topGrid), sender)).appendText("]"));
		}
	}

	private static DimensionalCoord getGridCoords(Grid grid)
	{
		Collection<IGridNode> controllers = grid.getMachinesFast(TileController.class);
		for (IGridNode controller : controllers)
		{
			DimensionalCoord location = controller.getGridBlock().getLocation();
			if (location != null)
				return location;
		}

		for (IGridNode node : grid.getNodes())
		{
			DimensionalCoord location = node.getGridBlock().getLocation();
			if (location != null)
				return location;
		}

		return null;
	}

	private static <E> Collection<E> toCollection(Iterable<E> iterable)
	{
		return iterable instanceof Collection ? (Collection<E>) iterable : Lists.newArrayList(iterable);
	}
}
