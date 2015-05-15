package com.ugopiemontese.opendatalecce;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ugopiemontese.opendatalecce.utils.Incidenti;
import com.ugopiemontese.opendatalecce.utils.IncidentiSQLiteHelper;
import android.widget.AdapterView.OnItemSelectedListener;

public class MapActivity extends Activity implements OnCameraChangeListener {
	
	private static int status;
	static SharedPreferences prefs;
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mActivities;
	
	private GoogleMap map;
	private MapFragment mapFragment;
		
	AsyncTask<Void, Void, Boolean> TaskAsincrono = null;
	
	private Double lecce_lat = 40.352011;
	private Double lecce_lng = 18.169139;
    
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    
    private static final String TAG_ADDRESS = "Strada";
    private static final String TAG_TOTALE = "Totale incidenti";
    private static final String TAG_COINVOLTI = "Persone coinvolte";
    private static final String TAG_ILLESI = "Illesi";
    private static final String TAG_MORTI = "Morti";
    private static final String TAG_PROGNOSI = "Prognosi Riservate";
    private static final String TAG_FERITI = "Feriti";
    
    private static final int TAG_VERSIONE_DATI = 2;
        
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		mActivities = getResources().getStringArray(R.array.activities);
		
		getActionBar().setTitle(mActivities[0]);
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mActivities));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setItemChecked(0, true);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {
        	
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(R.string.activity_map_title);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }
            
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		if (savedInstanceState == null) {
			
			final Spinner type = (Spinner) findViewById(R.id.spinnerType);
			type.setOnItemSelectedListener(new OnItemSelectedListener() {
				
				int currentRequiredInfo = type.getSelectedItemPosition();
				
	 		    @Override
	 		    public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
	 		    	if ( currentRequiredInfo != position ) {
	 		    		currentRequiredInfo = position;
	 		    		
	 		    		if (status == ConnectionResult.SUCCESS) {

	 			 			if (prefs.getBoolean("primo_avvio", true) || (prefs.getInt("versione_dati", 1) < TAG_VERSIONE_DATI)) {

								if (isOnline()) {

									TaskAsincrono = new PrimoAvvioAsincrono().execute();

								} else {

									new AlertDialog.Builder(getApplicationContext())
										.setIcon(R.drawable.ic_launcher)
										.setTitle(android.R.string.dialog_alert_title)
										.setMessage(R.string.primoavvio_no_network_description)
										.setCancelable(false)
										.setPositiveButton(R.string.primoavvio_no_network_settings, new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												Intent settings = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
												startActivity(settings);
											}
										})
										.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												finish();
											}
										}).show();

								}


	 			 			} else {

	 			 				TaskAsincrono = new CaricamentoAsincrono().execute();

	 			 			}

	 			 		}
	 		    		
	 		    	}
	 		    }

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// DO NOTHING
				}

	 		});
			
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();

		status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (status == ConnectionResult.SUCCESS) {

			if (prefs.getBoolean("primo_avvio", true) || (prefs.getInt("versione_dati", 1) < TAG_VERSIONE_DATI)) {

				if (isOnline()) {

					TaskAsincrono = new PrimoAvvioAsincrono().execute();

				} else {

					new AlertDialog.Builder(this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle(android.R.string.dialog_alert_title)
							.setMessage(R.string.primoavvio_no_network_description)
							.setCancelable(false)
							.setPositiveButton(R.string.primoavvio_no_network_settings, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									Intent settings = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
									startActivity(settings);
								}
							})
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							}).show();

				}

			} else {

				TaskAsincrono = new CaricamentoAsincrono().execute();

			}

		} else {

			GooglePlayServicesUtil.getErrorDialog(status, this, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.global, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		
		int id = item.getItemId();
		if ( id == R.id.action_credits ) {
			Intent act = new Intent(MapActivity.this, AboutActivity.class);
			startActivity(act);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        mDrawerList.setItemChecked(position, true);
        getActionBar().setTitle(mActivities[position]);
        
        Intent act = null;
        
        switch (position) {
        	case 1:
        		act = new Intent(MapActivity.this, NaturaGraphActivity.class);
        		break;
        	case 2:
        		act = new Intent(MapActivity.this, OrariGraphActivity.class);
        		break;
        }
        
        if ( act != null ) {
        	startActivity(act);
        }
        
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
	public void onDestroy() {
		super.onDestroy();
		if (TaskAsincrono != null && TaskAsincrono.getStatus() != AsyncTask.Status.FINISHED) {
			TaskAsincrono.cancel(true);
		}
	}
    
	private void InitMap() {		
	    mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView));
	    map = mapFragment.getMap();
	    map.clear();
	    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	    map.setMyLocationEnabled(false);
	    map.getUiSettings().setAllGesturesEnabled(false);

	    LatLng lecce = new LatLng(Double.valueOf(lecce_lat), Double.valueOf(lecce_lng));
	    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lecce, 12);
		if (map != null) {
		    map.animateCamera(cameraUpdate);
		}
		
		map.setOnCameraChangeListener(this);
		map.getUiSettings().setMapToolbarEnabled(false);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.getUiSettings().setZoomGesturesEnabled(true);
	    map.getUiSettings().setScrollGesturesEnabled(true);
	    map.getUiSettings().setRotateGesturesEnabled(true);
	}
	
	@Override
	public void onCameraChange(CameraPosition position) {
		
		boolean min_lat = position.target.latitude <= lecce_lat - 0.15;
		boolean max_lat = position.target.latitude >= lecce_lat + 0.15;
		boolean min_lng = position.target.longitude <= lecce_lng - 0.15;
		boolean max_lng = position.target.longitude >= lecce_lng + 0.15;
		boolean min_zoom = map.getCameraPosition().zoom < 12;
		
		//if out of bounds, restore center and zoom
		if ((min_lat && min_lng) || (min_lat && max_lng) || (max_lat && max_lng) || (max_lat && min_lng) || min_zoom) {
			LatLng lecce = new LatLng(Double.valueOf(lecce_lat), Double.valueOf(lecce_lng));
		    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lecce, 12);
			if (map != null) {
			    map.animateCamera(cameraUpdate);
			}
		}
		
	}
	
	private Bitmap setMarkerDrawable(int number) {
		
		int background = R.drawable.low;

		int LOW = 5;
		int MED = 9;
				
		Spinner type = (Spinner) findViewById(R.id.spinnerType);
		
		if (type.getSelectedItemPosition() == 2) { //PROGNOSI RISERVATE
			LOW = 0;
			MED = 2;
		} else if (type.getSelectedItemPosition() == 5) { //MORTI
			LOW = 0;
			MED = 0;
		}
		
		if (number < LOW) {
			background = R.drawable.low;
		} else if (number < MED) {
			background = R.drawable.medium;
		} else {
			background = R.drawable.high;
		}
			
		Bitmap icon = drawTextToBitmap(background, String.valueOf(number));
		
	    return icon;
	    
	}

	private Bitmap drawTextToBitmap(int gResId, String gText) {
		
	  Resources resources = getResources();
	  float scale = resources.getDisplayMetrics().density;
	  
	  Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);
	  android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
	  
	  if (bitmapConfig == null) {
		  bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
	  }
	  bitmap = bitmap.copy(bitmapConfig, true);
	  Canvas canvas = new Canvas(bitmap);
	  
	  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	  paint.setColor(Color.rgb(255, 255, 2255));
	  paint.setTextSize((int) (15 * scale));
	  paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);
	 
	  Rect bounds = new Rect();
	  paint.getTextBounds(gText, 0, gText.length(), bounds);
	  int x = (bitmap.getWidth() - bounds.width())/2;
	  int y = (bitmap.getHeight() + bounds.height())/2;
	  canvas.drawText(gText, x, y, paint);
	 
	  return bitmap;
	  
	}
    
	private String loadJSONFromAsset() {
		
	    String json = null;
	    try {
	        InputStream is = getAssets().open("statisticheincidenti2013.json");
	        int size = is.available();
	        byte[] buffer = new byte[size];
	        is.read(buffer);
	        is.close();
	        json = new String(buffer, "UTF-8");
	    } catch (IOException ex) {
	        ex.printStackTrace();
	        return null;
	    }
	    
	    return json;

	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
	
    private class PrimoAvvioAsincrono extends AsyncTask<Void, Void, Boolean> {
		
    	IncidentiSQLiteHelper db = new IncidentiSQLiteHelper(getBaseContext());
    	RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
    	TextView loadingText = (TextView) findViewById(R.id.loadingText);
    	Spinner type = (Spinner) findViewById(R.id.spinnerType);
    	
		@Override
		protected void onPreExecute() {
			
			type.setEnabled(false);
			loading.setVisibility(View.VISIBLE);
			loadingText.setText(R.string.loading_text_primoavvio);
			InitMap();
			
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			JSONArray incidenti = null;
			try {
				
				incidenti = new JSONArray(loadJSONFromAsset());
				Geocoder geocoder = new Geocoder(getBaseContext());
				List<Address> dati = null;
				
				for (int i = 0; i < incidenti.length(); i++) {
					
					JSONObject loc = incidenti.getJSONObject(i);
					String address = loc.getString(TAG_ADDRESS);
					int totale = loc.getInt(TAG_TOTALE);
					int coinvolti = loc.getInt(TAG_COINVOLTI);
					int illesi = loc.getInt(TAG_ILLESI);
					int morti = loc.getInt(TAG_MORTI);
					int prognosi = loc.getInt(TAG_PROGNOSI);
					int feriti = loc.getInt(TAG_FERITI);
					
					dati = geocoder.getFromLocationName(address + ", Lecce, Italia", 1);
					
					if (!dati.isEmpty()) {
						Address pos = dati.get(0);
		                db.addIncidenti(new Incidenti(
		                		(String) Html.fromHtml(address).toString(), 
		                		(Double) pos.getLatitude(), 
		                		(Double) pos.getLongitude(), 
		                		(int) totale, 
		                		(int) coinvolti,
		                		(int) illesi, 
		                		(int) morti, 
		                		(int) prognosi, 
		                		(int) feriti
		                ));
					}

				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if ( db.getIncidentiCount() > 0 ) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			
			if (isCancelled()) {
				return;
			}
			
			if (success) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("primo_avvio", false);
				editor.putInt("versione_dati", TAG_VERSIONE_DATI);
				editor.commit();
			}
			
			List<Incidenti> incidentiList = db.getAllIncidenti();
			
			for (int count = 0; count < incidentiList.size(); count++) {	
				LatLng point = new LatLng ((Double) incidentiList.get(count).getLat(), (Double) incidentiList.get(count).getLng());
				@SuppressWarnings("unused")
				Marker mrk = map.addMarker(new MarkerOptions()
					.title((String) incidentiList.get(count).getAddress())
		    		.position(point)
		    		.icon(BitmapDescriptorFactory.fromBitmap(setMarkerDrawable((int) incidentiList.get(count).getTotale())))
		    		.anchor(0.5f, 0.5f)
		    	);
			}
			
			loading.setVisibility(View.GONE);
			type.setEnabled(true);
			
		}
		
    }
    
    private class CaricamentoAsincrono extends AsyncTask<Void, Void, Boolean> {
		
    	IncidentiSQLiteHelper db = new IncidentiSQLiteHelper(getBaseContext());
    	RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
    	TextView loadingText = (TextView) findViewById(R.id.loadingText);
    	Spinner type = (Spinner) findViewById(R.id.spinnerType);
    	
		@Override
		protected void onPreExecute() {
			
			type.setEnabled(false);
			loading.setVisibility(View.VISIBLE);
			loadingText.setText(R.string.loading_text_avvio);
			InitMap();
			
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			return true;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			
			if (isCancelled()) {
				return;
			}

			List<Incidenti> incidentiList = db.getAllIncidenti();
			
			int currentRequiredInfo = type.getSelectedItemPosition();
			
			for (int count = 0; count < incidentiList.size(); count++) {	
				
				int numericData = 0;
				
				switch (currentRequiredInfo) {
					case 0:
						numericData = (int) incidentiList.get(count).getTotale();
						break;
					case 1:
						numericData = (int) incidentiList.get(count).getCoinvolti();
						break;
					case 2:
						numericData = (int) incidentiList.get(count).getPrognosi();
						break;
					case 3:
						numericData = (int) incidentiList.get(count).getIllesi();
						break;
					case 4:
						numericData = (int) incidentiList.get(count).getFeriti();
						break;
					case 5:
						numericData = (int) incidentiList.get(count).getMorti();
						break;
					default:
						numericData = 0;
						break;
				}
				
				if (numericData > 0) {
					LatLng point = new LatLng ((Double) incidentiList.get(count).getLat(), (Double) incidentiList.get(count).getLng());
					@SuppressWarnings("unused")
					Marker mrk = map.addMarker(new MarkerOptions()
						.title((String) incidentiList.get(count).getAddress())
			    		.position(point)
			    		.icon(BitmapDescriptorFactory.fromBitmap(setMarkerDrawable( (int) numericData )))
			    		.anchor(0.5f, 0.5f)
			    	);
				}
				
			}
			
			loading.setVisibility(View.GONE);
			type.setEnabled(true);
			
		}
		
    }

}
