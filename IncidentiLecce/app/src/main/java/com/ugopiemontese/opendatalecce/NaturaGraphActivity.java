package com.ugopiemontese.opendatalecce;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class NaturaGraphActivity extends Activity {
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mActivities;
    
    AsyncTask<Void, Void, Boolean> TaskAsincrono = null;
    
    private static final String TAG_TOTALE = "Totale incidenti";
    private static final String TAG_COINVOLTI = "Persone coinvolte";
    private static final String TAG_ILLESI = "Illesi";
    private static final String TAG_MORTI = "Morti";
    private static final String TAG_PROGNOSI = "Prognosi Riservate";
    private static final String TAG_FERITI = "Feriti";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph_natura);
		
		mActivities = getResources().getStringArray(R.array.activities);
		
		getActionBar().setTitle(mActivities[1]);
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mActivities));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setItemChecked(1, true);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {
        	
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(R.string.activity_map_title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        Spinner type = (Spinner) findViewById(R.id.spinnerType);
		type.setOnItemSelectedListener(new OnItemSelectedListener() {
						
 		    @Override
 		    public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
 			 	TaskAsincrono = new CaricamentoAsincrono().execute();
 		    }
 		    
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//DOES NOTHING
			}
			
 		});
        
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
		if (id == R.id.action_credits) {
			Intent act = new Intent(NaturaGraphActivity.this, AboutActivity.class);
			startActivity(act);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* The click listener for ListView in the navigation drawer */
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
        	case 0:
        		act = new Intent(NaturaGraphActivity.this, MapActivity.class);
        		break;
        	case 2:
        		act = new Intent(NaturaGraphActivity.this, OrariGraphActivity.class);
        		break;
        }
        
        if (act != null) {
        	startActivity(act);
        }
        
        mDrawerLayout.closeDrawer(mDrawerList);
    }

	@Override
	protected void onResume() {
		super.onResume();
		TaskAsincrono = new CaricamentoAsincrono().execute();
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
    
    private String loadJSONFromAsset() {
		
	    String json = null;
	    try {
	        InputStream is = getAssets().open("statistichepernaturaincidenti2013.json");
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
    
    private class CaricamentoAsincrono extends AsyncTask<Void, Void, Boolean> {
    	
    	RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
    	TextView loadingText = (TextView) findViewById(R.id.loadingText);
    	Spinner type = (Spinner) findViewById(R.id.spinnerType);
    	LinearLayout layout = (LinearLayout) findViewById(R.id.layout_graph);
    	TextView totaleText = (TextView) findViewById(R.id.textTotale);
    	
    	ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();
    	
    	@Override
		protected void onPreExecute() {
			
			type.setEnabled(false);
			loading.setVisibility(View.VISIBLE);
			loadingText.setText(R.string.loading_text_avvio);
			layout.removeAllViews();
			
		}
    	
    	@Override
		protected Boolean doInBackground(Void... params) {
			
			JSONArray incidenti = null;
			try {
				
				incidenti = new JSONArray(loadJSONFromAsset());
				
				for (int i = 0; i < incidenti.length(); i++) {
					
					JSONObject loc = incidenti.getJSONObject(i);
					
					ArrayList<Object> nat = new ArrayList<Object> ();
					nat.add((int) loc.getInt(TAG_TOTALE));
					nat.add((String) loc.getString(TAG_COINVOLTI));
					nat.add((String) loc.getString(TAG_ILLESI));
					nat.add((String) loc.getString(TAG_MORTI));
					nat.add((String) loc.getString(TAG_PROGNOSI));
					nat.add((String) loc.getString(TAG_FERITI));
										
					data.add(nat);
					
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
			
		}
    	
    	@Override
		protected void onPostExecute(final Boolean success) {
			
			if (isCancelled()) {
				return;
			}
						
			ArrayList<Object> serie = data.get((int) type.getSelectedItemPosition());
			GraphViewData[] natura = new GraphViewData[5];
			
			for (int count = 1; count < serie.size(); count++) {
				natura[count - 1] = new GraphViewData(count, Double.valueOf(serie.get(count).toString()));
			}
	         
	        GraphView graphView = new BarGraphView(
	            getBaseContext(),
	            ""
	        );
	        	        
	        graphView.addSeries(new GraphViewSeries(natura));
	        
	        graphView.setHorizontalLabels(new String[] {
        		TAG_COINVOLTI,
        		TAG_ILLESI,
        		TAG_MORTI,
        		TAG_PROGNOSI,
        		TAG_FERITI
	        });
	        
	        graphView.getGraphViewStyle().setGridColor(Color.TRANSPARENT);
	        graphView.getGraphViewStyle().setVerticalLabelsColor(Color.TRANSPARENT);
	        graphView.getGraphViewStyle().setVerticalLabelsWidth(1);
	        graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
	        graphView.getGraphViewStyle().setNumHorizontalLabels(5);
	        ((BarGraphView) graphView).setDrawValuesOnTop(true);
	        ((BarGraphView) graphView).setValuesOnTopColor(Color.BLACK);
	        graphView.setScrollable(false);
	        graphView.setScalable(false);
	        graphView.setManualYAxisBounds(350, 0);
	        	        
	        layout.addView(graphView);
	        totaleText.setText(serie.get(0).toString());
	        
			loading.setVisibility(View.GONE);
			type.setEnabled(true);
			
		}
    	
    }
	
}
