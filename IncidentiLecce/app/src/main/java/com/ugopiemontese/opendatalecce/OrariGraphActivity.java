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
import android.widget.TextView;

public class OrariGraphActivity extends Activity {
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mActivities;
    
    AsyncTask<Void, Void, Boolean> TaskAsincrono = null;
    
    private static final String TAG_TOTALE = "Totale";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph_orari);
		
		mActivities = getResources().getStringArray(R.array.activities);
		
		getActionBar().setTitle(mActivities[2]);
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mActivities));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setItemChecked(2, true);
        
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
			Intent act = new Intent(OrariGraphActivity.this, AboutActivity.class);
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
        	case 0:
        		act = new Intent(OrariGraphActivity.this, MapActivity.class);
        		break;
        	case 1:
        		act = new Intent(OrariGraphActivity.this, NaturaGraphActivity.class);
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
	        InputStream is = getAssets().open("statisticheperoraincidenti2013.json");
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
    	LinearLayout layout = (LinearLayout) findViewById(R.id.layout_graph);
    	
    	ArrayList<Object> data = new ArrayList<Object>();
    	
    	@Override
		protected void onPreExecute() {
			
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
					
					data.add((String) loc.getString(TAG_TOTALE));
										
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
						
			GraphViewData[] orari = new GraphViewData[24];
			
			for (int count = 0; count < data.size(); count++) {
				orari[count] = new GraphViewData(count, Double.valueOf(data.get(count).toString()));
			}
	         
	        GraphView graphView = new BarGraphView(
	            getBaseContext(),
	            ""
	        );
	        	        
	        graphView.addSeries(new GraphViewSeries(orari));
	        
	        graphView.setHorizontalLabels(new String[] {
	        	"01", "02", "03", "04", "05", "06",
	        	"07", "08", "09", "10", "11", "12",
	        	"13", "14", "15", "16", "17", "18",
	        	"19", "20", "21", "22", "23", "24"
	        });
	        
	        graphView.getGraphViewStyle().setGridColor(Color.TRANSPARENT);
	        graphView.getGraphViewStyle().setVerticalLabelsColor(Color.TRANSPARENT);
	        graphView.getGraphViewStyle().setVerticalLabelsWidth(1);
	        graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
	        ((BarGraphView) graphView).setDrawValuesOnTop(true);
	        ((BarGraphView) graphView).setValuesOnTopColor(Color.BLACK);
	        graphView.setScrollable(false);
	        graphView.setScalable(false);
	        graphView.setManualYAxisBounds(50, 0);
	        	        
	        layout.addView(graphView);
			loading.setVisibility(View.GONE);
			
		}
    	
    }
	
}
