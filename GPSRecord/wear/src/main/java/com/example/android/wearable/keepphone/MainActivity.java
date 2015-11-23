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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import net.mkcz.mobile.android.gpsrecord.R;

public class MainActivity extends Activity implements WatchViewStub.OnLayoutInflatedListener,
                                                      View.OnClickListener, View.OnLongClickListener,
                                                      GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                                                      Runnable, GPSReceiver.Listener

{
	private static final int LOCATION_REQUEST_INTERVAL_MILLIS = 100;
	private static final int CHRONOMETER_UI_INTERVAL_MILLIS = 1000;

	private TextView m_timerText;
	private TextView m_latitude;
	private TextView m_longitude;
	private TextView m_speed;
	private TextView m_avg_speed;

	private GoogleApiClient m_googleApiClient;
	private State m_state;
	private Chronometer m_chronometer;
	private GPSReceiver m_gpsReceiver;
	private Handler m_handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		stub.setOnLayoutInflatedListener(this);
		stub.setOnClickListener(this);
		stub.setOnLongClickListener(this);
		this.m_googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		this.m_state = new State();
		this.m_googleApiClient.connect();
		this.m_chronometer = new Chronometer(this.m_state);
		this.m_gpsReceiver = new GPSReceiver(this.m_state, this.m_googleApiClient, this, LOCATION_REQUEST_INTERVAL_MILLIS,
		                                     LocationRequest.PRIORITY_HIGH_ACCURACY);
		this.m_handler = new Handler();
	}

	@Override
	public void onLayoutInflated(final WatchViewStub watchViewStub)
	{
		this.m_timerText = (TextView) watchViewStub.findViewById(R.id.chronos);
		this.m_latitude = (TextView) watchViewStub.findViewById(R.id.value_latitude);
		this.m_longitude = (TextView) watchViewStub.findViewById(R.id.value_longitude);
		this.m_speed = (TextView) watchViewStub.findViewById(R.id.value_speed);
		this.m_avg_speed = (TextView) watchViewStub.findViewById(R.id.value_avg_speed);
	}

	@Override
	public void onClick(View v)
	{
		if (this.m_state.isStopped())
		{
			this.m_state.start();
			this.m_gpsReceiver.reset();
			this.m_gpsReceiver.run();
			this.m_chronometer.reset();
			this.runChronometerUIUpdate();
			this.m_chronometer.run();
		}
		else
		{
			if (this.m_state.isPaused())
			{
				this.m_state.resume();
				this.m_gpsReceiver.run();
				this.runChronometerUIUpdate();
				this.m_chronometer.run();
			}
			else
			{
				this.m_state.pause();
			}
		}
	}

	@Override
	public boolean onLongClick(View v)
	{
		if (this.m_state.isStopped())
		{
			this.m_state.start();
			this.m_gpsReceiver.reset();
			this.m_gpsReceiver.run();
			this.m_chronometer.reset();
			this.runChronometerUIUpdate();
			this.m_chronometer.run();
		}
		else
		{
			this.m_state.stop();
		}
		return false;
	}

	@Override
	public void onConnected(Bundle bundle)
	{
		if (this.m_state.isPaused() || this.m_state.isAutoPaused())
		{
			this.m_state.resume();
		}
	}

	@Override
	public void onConnectionSuspended(int i)
	{
		if (this.m_state.isRunning())
		{
			this.m_state.pause(true);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult)
	{
		if (this.m_state.isRunning())
		{
			this.m_state.pause(true);
		}
	}

	@Override
	public void run()
	{
		this.runChronometerUIUpdate();
	}

	private void runChronometerUIUpdate()
	{
		this.m_timerText.setText(this.m_chronometer.getActiveTime());
		if (this.m_state.isRunning())
		{
			this.m_handler.postDelayed(this, CHRONOMETER_UI_INTERVAL_MILLIS);
		}
	}

	@Override
	public void onLocationChanged(double latitude, double longitude, float speed, float speedAverage)
	{
		this.m_latitude.setText(String.format("%02.3f", latitude));
		this.m_longitude.setText(String.format("%02.3f", longitude));
		this.m_speed.setText(String.format("%02.2f m/s", speed));
		this.m_avg_speed.setText(String.format("%02.2f m/s", speedAverage));
	}
}