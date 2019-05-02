package com.gamerforea.ae.util;

import java.util.Arrays;

public final class LongRingBuffer
{
	private final long[] array;
	private int nextIndex;

	public LongRingBuffer(int size)
	{
		if (size <= 0)
			throw new IllegalArgumentException("size must be positive");
		this.array = new long[size];
	}

	public long getAverage()
	{
		long sum = 0;
		for (long value : this.array)
		{
			sum += value;
		}
		return sum / this.array.length;
	}

	public int getSize()
	{
		return this.array.length;
	}

	public void push(long value)
	{
		int index = this.nextIndex;
		this.array[index] = value;

		int nextIndex = index + 1;
		this.nextIndex = nextIndex == this.array.length ? 0 : nextIndex;
	}

	public void clear()
	{
		Arrays.fill(this.array, 0);
	}
}
