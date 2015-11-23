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

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by mikec on 28/02/15.
 */
public class GPSReceiver implements Runnable, LocationListener
{
	public static final int GPS_RECEIVER_RETRY_DELAY_MILLIS = 500;
	public static final String GPS = "GPS";
	private final GoogleApiClient m_locationClient;
	private final LocationRequest m_locationRequest;
	private final Listener m_listener;
	private final Handler m_handler;
	private final State m_state;

	private long m_speedRecordingNumber;
	private float m_speedAverage;
	private boolean m_locationUpdatesRequested;

	public GPSReceiver(final State state, final GoogleApiClient locationClient, final Listener listener, final long locationRequestIntervalMillis,
	                   final int priority)
	{
		this.m_state = state;
		this.m_locationClient = locationClient;
		this.m_listener = listener;
		this.m_locationRequest = LocationRequest.create()
		                                        .setPriority(priority)
		                                        .setInterval(locationRequestIntervalMillis);
		this.m_handler = new Handler();
		this.reset();
	}

	@Override
	public void run()
	{
		Log.i(GPS, "run");
		if (this.m_locationClient.isConnected() && this.m_state.isRunning())
		{
			Log.i(GPS, "connected, running");
			this.stop();
			LocationServices.FusedLocationApi
					.requestLocationUpdates(this.m_locationClient, this.m_locationRequest, this)
					.setResultCallback(
							new ResultCallback<Status>()
							{
								@Override
								public void onResult(Status status)
								{
									if (status.isSuccess())
									{
										Log.i(GPS, "request_success");
										GPSReceiver.this.m_locationUpdatesRequested = true;
									}
									else
									{
										Log.i(GPS, "request_fail, retrying in .5");
										GPSReceiver.this.m_handler.postDelayed(GPSReceiver.this, GPS_RECEIVER_RETRY_DELAY_MILLIS);
									}
								}
							}
					);
		}
		else
		{
			Log.i(GPS, String.format("cli: %b, run: %b, retrying in .5", this.m_locationClient.isConnected(), this.m_state.isRunning()));
			this.m_handler.postDelayed(GPSReceiver.this, GPS_RECEIVER_RETRY_DELAY_MILLIS);
		}
	}

	public void reset()
	{
		this.m_speedRecordingNumber = 0;
		this.m_speedAverage = 0.0f;
	}

	public void stop()
	{
		if (this.m_locationUpdatesRequested)
		{
			LocationServices.FusedLocationApi.removeLocationUpdates(this.m_locationClient, this);
			this.m_locationUpdatesRequested = false;
		}
	}

	@Override
	public void onLocationChanged(Location location)
	{
		Log.i(GPS, "update");
		if (location != null)
		{
			Log.i("GPS_DEBUG", location.toString());
		}
		if (this.m_state.isRunning() && location != null)
		{
			Log.i(GPS, "running and valid");
			if (this.m_speedRecordingNumber == 0 || this.m_speedRecordingNumber == 1)
			{
				this.m_speedAverage += location.getSpeed();
			}
			else
			{
				this.m_speedAverage *= this.m_speedRecordingNumber;
				this.m_speedAverage += location.getSpeed();
			}
			this.m_speedRecordingNumber++;
			this.m_speedAverage /= this.m_speedRecordingNumber;
			Log.i(GPS, String.format("Values{lat:%02.3f, long: %02.3f, spd: %02.2f m/s, avg: %02.2f m/s, numRecs: %d",
			                         location.getLatitude(),
			                         location.getLongitude(),
			                         location.getSpeed(),
			                         this.m_speedAverage,
			                         this.m_speedRecordingNumber));
			this.m_listener.onLocationChanged(location.getLatitude(), location.getLongitude(), location.getSpeed(), this.m_speedAverage);
		}
	}


	public static interface Listener
	{
		public void onLocationChanged(double latitude, double longitude, float speed, float speedAverage);
	}
}
