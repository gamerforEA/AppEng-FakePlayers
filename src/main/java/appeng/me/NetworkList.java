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

package appeng.me;

import com.gamerforea.ae.EventConfig;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NetworkList implements Collection<Grid>
{
	private List<Grid> networks = new LinkedList<>();

	// TODO gamerforEA code start
	private boolean iteratorCreated;

	@Override
	public boolean removeIf(Predicate<? super Grid> filter)
	{
		this.copy();
		return this.networks.removeIf(filter);
	}

	@Override
	public Spliterator<Grid> spliterator()
	{
		return this.networks.spliterator();
	}

	@Override
	public Stream<Grid> stream()
	{
		return this.networks.stream();
	}

	@Override
	public Stream<Grid> parallelStream()
	{
		return this.networks.parallelStream();
	}

	@Override
	public void forEach(Consumer<? super Grid> action)
	{
		this.networks.forEach(action);
	}
	// TODO gamerforEA code end

	@Override
	public int size()
	{
		return this.networks.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.networks.isEmpty();
	}

	@Override
	public boolean contains(final Object o)
	{
		return this.networks.contains(o);
	}

	@Override
	public Iterator<Grid> iterator()
	{
		// TODO gamerforEA code start
		this.iteratorCreated = true;
		// TODO gamerforEA code end

		return this.networks.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return this.networks.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a)
	{
		return this.networks.toArray(a);
	}

	@Override
	public boolean add(final Grid e)
	{
		this.copy();
		return this.networks.add(e);
	}

	@Override
	public boolean remove(final Object o)
	{
		this.copy();
		return this.networks.remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return this.networks.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends Grid> c)
	{
		this.copy();
		return this.networks.addAll(c);
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		this.copy();
		return this.networks.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		this.copy();
		return this.networks.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.networks = new LinkedList<>();
	}

	private void copy()
	{
		/* TODO gamerforEA code replace, old code:
		final List<Grid> old = this.networks;
		this.networks = new LinkedList<>();
		this.networks.addAll(old); */
		if (EventConfig.preventCopyOnWriteNetworkList && !this.iteratorCreated)
			return;
		this.networks = new LinkedList<>(this.networks);
		this.iteratorCreated = false;
		// TODO gamerforEA code end
	}
}
