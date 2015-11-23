/*
 * Copyright 2015 Mihnea Cinteza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.keepphone;

import android.os.Handler;

/**
 * Created by mikec on 22/02/15.
 */
public class Chronometer implements Runnable
{
	private static final long CHRONOMETER_INTERVAL_MILLIS = 1000;

	private final ElapsedTime m_activeTime;
	private final ElapsedTime m_totalTime;
	private final Handler m_handler;
	private final State m_state;

	public Chronometer(final State state)
	{
		this.m_state = state;
		this.m_handler = new Handler();
		this.m_activeTime = new ElapsedTime();
		this.m_totalTime = new ElapsedTime();
		this.reset();
	}

	@Override
	public void run()
	{
		if (this.m_state.isRunning())
		{
			this.m_activeTime.tick();
		}
		this.m_totalTime.tick();
		if (this.m_state.isRunning() || this.m_state.isAutoPaused())
		{
			this.m_handler.postDelayed(this, CHRONOMETER_INTERVAL_MILLIS);
		}
	}

	public void reset()
	{
		this.m_activeTime.reset();
		this.m_totalTime.reset();
	}

	public String getActiveTime()
	{
		return this.m_activeTime.toString();
	}

	public String getTotalTime()
	{
		return this.m_totalTime.toString();
	}

	private static class ElapsedTime
	{
		private long m_elapsedTime;
		private long m_lastTickTime;

		public ElapsedTime()
		{
			this.reset();
		}

		public void tick()
		{
			if (this.m_lastTickTime == 0L)
			{
				this.m_lastTickTime = System.nanoTime();
				this.m_elapsedTime = 0L;
			}
			else
			{
				final long currentTickTime = System.nanoTime();
				this.m_elapsedTime += (currentTickTime - this.m_lastTickTime);
				this.m_lastTickTime = currentTickTime;
			}
		}

		public void reset()
		{
			this.m_elapsedTime = 0L;
			this.m_lastTickTime = 0L;
		}

		@Override
		public String toString()
		{
			final long currentElapsedTime = this.m_elapsedTime;
			final long fromNanos = currentElapsedTime / 1000000000;
			final long seconds = fromNanos % 60;
			final long minutes = (fromNanos / 60) % 60;
			final long hours = fromNanos / 3600;
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		}
	}
}
