/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.util.item;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import com.gamerforea.ae.EventConfig;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class ItemList implements IItemList<IAEItemStack>
{
	private final NavigableMap<IAEItemStack, IAEItemStack> records = new ConcurrentSkipListMap<>();

	// TODO gamerforEA code start
	private final Map<IAEItemStack, IAEItemStack> unorderedRecords = EventConfig.useHybridItemList ? new ConcurrentHashMap<>() : this.records;
	// TODO gamerforEA code end

	@Override
	public void add(final IAEItemStack option)
	{
		if (option == null)
			return;

		// TODO gamerforEA code replace, old code:
		// final IAEItemStack st = this.records.get(option);
		final IAEItemStack st = this.unorderedRecords.get(option);
		// TODO gamerforEA code end

		if (st != null)
		{
			st.add(option);
			return;
		}

		final IAEItemStack opt = option.copy();
		this.putItemRecord(opt);
	}

	@Override
	public IAEItemStack findPrecise(final IAEItemStack itemStack)
	{
		if (itemStack == null)
			return null;

		// TODO gamerforEA code replace, old code:
		// return this.records.get(itemStack);
		return this.unorderedRecords.get(itemStack);
		// TODO gamerforEA code end
	}

	@Override
	public Collection<IAEItemStack> findFuzzy(final IAEItemStack filter, final FuzzyMode fuzzy)
	{
		if (filter == null)
			return Collections.emptyList();

		final AEItemStack ais = (AEItemStack) filter;

		if (ais.isOre())
		{
			final OreReference or = ais.getDefinition().getIsOre();
			final List<IAEItemStack> aeEquivalents = or.getAEEquivalents();

			if (aeEquivalents.size() == 1)
			{
				final IAEItemStack is = aeEquivalents.get(0);
				return this.findFuzzyDamage((AEItemStack) is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE);
			}

			final Collection<IAEItemStack> output = new LinkedList<>();

			for (final IAEItemStack is : aeEquivalents)
			{
				output.addAll(this.findFuzzyDamage((AEItemStack) is, fuzzy, is.getItemDamage() == OreDictionary.WILDCARD_VALUE));
			}

			return output;
		}

		return this.findFuzzyDamage(ais, fuzzy, false);
	}

	@Override
	public boolean isEmpty()
	{
		return !this.iterator().hasNext();
	}

	@Override
	public void addStorage(final IAEItemStack option)
	{
		if (option == null)
			return;

		// TODO gamerforEA code replace, old code:
		// final IAEItemStack st = this.records.get(option);
		final IAEItemStack st = this.unorderedRecords.get(option);
		// TODO gamerforEA code end

		if (st != null)
		{
			st.incStackSize(option.getStackSize());
			return;
		}

		final IAEItemStack opt = option.copy();

		this.putItemRecord(opt);
	}

	/*
	 * public void clean() { Iterator<StackType> i = iterator(); while (i.hasNext()) { StackType AEI =
	 * i.next(); if ( !AEI.isMeaningful() ) i.remove(); } }
	 */

	@Override
	public void addCrafting(final IAEItemStack option)
	{
		if (option == null)
			return;

		// TODO gamerforEA code replace, old code:
		// final IAEItemStack st = this.records.get(option);
		final IAEItemStack st = this.unorderedRecords.get(option);
		// TODO gamerforEA code end

		if (st != null)
		{
			st.setCraftable(true);
			return;
		}

		final IAEItemStack opt = option.copy();
		opt.setStackSize(0);
		opt.setCraftable(true);

		this.putItemRecord(opt);
	}

	@Override
	public void addRequestable(final IAEItemStack option)
	{
		if (option == null)
			return;

		// TODO gamerforEA code replace, old code:
		// final IAEItemStack st = this.records.get(option);
		final IAEItemStack st = this.unorderedRecords.get(option);
		// TODO gamerforEA code end

		if (st != null)
		{
			st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
			return;
		}

		final IAEItemStack opt = option.copy();
		opt.setStackSize(0);
		opt.setCraftable(false);
		opt.setCountRequestable(option.getCountRequestable());

		this.putItemRecord(opt);
	}

	@Override
	public IAEItemStack getFirstItem()
	{
		for (final IAEItemStack stackType : this)
		{
			return stackType;
		}

		return null;
	}

	@Override
	public int size()
	{
		return this.records.size();
	}

	@Override
	public Iterator<IAEItemStack> iterator()
	{
		// TODO gamerforEA code start
		if (this.unorderedRecords != this.records)
			return new MeaningfulItemHybridIterator<>(this.records, this.unorderedRecords);
		// TODO gamerforEA code end

		return new MeaningfulItemIterator<>(this.records.values().iterator());
	}

	@Override
	public void resetStatus()
	{
		for (final IAEItemStack i : this)
		{
			i.reset();
		}
	}

	private IAEItemStack putItemRecord(final IAEItemStack itemStack)
	{
		// TODO gamerforEA code start
		if (this.unorderedRecords != this.records)
			this.unorderedRecords.put(itemStack, itemStack);
		// TODO gamerforEA code end

		return this.records.put(itemStack, itemStack);
	}

	private Collection<IAEItemStack> findFuzzyDamage(final AEItemStack filter, final FuzzyMode fuzzy, final boolean ignoreMeta)
	{
		final IAEItemStack low = filter.getLow(fuzzy, ignoreMeta);
		final IAEItemStack high = filter.getHigh(fuzzy, ignoreMeta);
		final Collection<IAEItemStack> values = this.records.subMap(low, true, high, true).descendingMap().values();

		// TODO gamerforEA code start
		if (this.unorderedRecords != this.records)
			return Collections.unmodifiableCollection(values);
		// TODO gamerforEA code start

		return values;
	}

	// TODO gamerforEA code start
	private static final class MeaningfulItemHybridIterator<T extends IAEItemStack> implements Iterator<T>
	{
		// private final Map<T, T> parentPrimary;
		private final Map<T, T> parentSecondary;
		private final Iterator<T> parentPrimaryIterator;
		private T next;

		public MeaningfulItemHybridIterator(Map<T, T> parentPrimary, Map<T, T> parentSecondary)
		{
			// this.parentPrimary = parentPrimary;
			this.parentSecondary = parentSecondary;
			this.parentPrimaryIterator = parentPrimary.values().iterator();
		}

		@Override
		public boolean hasNext()
		{
			while (this.parentPrimaryIterator.hasNext())
			{
				this.next = this.parentPrimaryIterator.next();

				if (this.next.isMeaningful())
					return true;

				this.parentPrimaryIterator.remove(); // self cleaning :3
				this.parentSecondary.remove(this.next);
			}

			this.next = null;
			return false;
		}

		@Override
		public T next()
		{
			if (this.next == null)
				throw new NoSuchElementException();
			return this.next;
		}

		@Override
		public void remove()
		{
			if (this.next == null)
			{
				if (this.parentPrimaryIterator.hasNext())
					this.next = this.parentPrimaryIterator.next();
				else
					throw new NoSuchElementException();
			}

			this.parentPrimaryIterator.remove();
			this.parentSecondary.remove(this.next);
		}
	}
	// TODO gamerforEA code end
}
