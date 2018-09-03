package devindow.GeoMeasure;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class GeoMeasure extends Activity {

    private LocationsDbAdapter mDbHelper;
	
    private LocationManager locationManager;
    private LocationIntentReceiver intentReceiver = new LocationIntentReceiver();
    private static final String LOCATION_CHANGED_ACTION = new String("android.intent.action.LOCATION_CHANGED");     
    private IntentFilter intentFilter = new IntentFilter(LOCATION_CHANGED_ACTION);
	private Intent intent = new Intent(LOCATION_CHANGED_ACTION);
	private Location toLocation;
	
	private CompassView compassView = new CompassView(this);
	
	
	// onCreate
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        addContentView(compassView, new android.widget.RelativeLayout.LayoutParams(-2, -2));

        
        mDbHelper = new LocationsDbAdapter(this);
        mDbHelper.open();
        fillData();
        
        
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        registerReceiver(intentReceiver, intentFilter);
 
		
        final Spinner spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
        spinnerTo.setOnItemSelectedListener(new OnItemSelectedListener() { 
            public void onItemSelected(AdapterView parent, View v, int position, long id) {
                final Spinner spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
            	Object object = spinnerTo.obtainItem(spinnerTo.getSelectedItemPosition());
            	Cursor cursor = (Cursor) object;
            	
            	toLocation = new Location();
            	toLocation.setLatitude(Double.parseDouble(cursor.getString(2)));
            	toLocation.setLongitude(Double.parseDouble(cursor.getString(3)));

               	update();
            }
    
            public void onNothingSelected(AdapterView parent) {
            	toLocation = null;
               	setDistances(0);               	
              }
        });
    }
    
    
    // Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean supRetVal = super.onCreateOptionsMenu(menu);
		menu.add(0, 0, "Save Here As...");
		menu.add(0, 1, "Enter Location...");
		menu.add(0, 2, "Delete Location");
		menu.add(0, 3, "Lookup Contact");
		return supRetVal;
	}
	
	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()) {
			case 0: // Save Here As...
			{
    	    	final Dialog dialog = new Dialog(GeoMeasure.this);
    	     	dialog.setContentView(R.layout.input_dialog);
    	    	dialog.setTitle("Name of location");
    	        Button button = (Button) dialog.findViewById(R.id.ok);
    	        button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
            	        EditText editText = (EditText) dialog.findViewById(R.id.name);

            	    	Location myLocation = locationManager.getCurrentLocation("gps");
            	    	double latitude = myLocation.getLatitude();
            	    	double longitude = myLocation.getLongitude();

            	        mDbHelper.createLocation(editText.getText().toString(), latitude, longitude);
            	    	
            	    	fillData();

                    	dialog.dismiss();
                    }
                });
    	    	dialog.show();
				return true;
			}
				
			case 1: // Enter Location
			{
    	    	final Dialog dialog = new Dialog(GeoMeasure.this);
    	     	dialog.setContentView(R.layout.input_dialog);
    	    	dialog.setTitle("Location");
    	        Button button = (Button) dialog.findViewById(R.id.ok);
    	        button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
            	        EditText editText = (EditText) dialog.findViewById(R.id.name);

            	    	Location myLocation = locationManager.getCurrentLocation("gps");
            	    	double latitude = myLocation.getLatitude();
            	    	double longitude = myLocation.getLongitude();

            	    	Geocoder geocoder = new Geocoder();
            	        Address[] results = null;
            	        try {
            	        	results = geocoder.getFromLocationName(editText.getText().toString(), latitude, longitude, 180, 360);
            	        }
            	        catch (Exception ex) { }

            	        if (results != null && results.length > 0) {
            	        	mDbHelper.createLocation(editText.getText().toString(), results[0].getLatitude(), results[0].getLongitude());
            	        	fillData();
            	        }
            	        else 
            	        	Toast.makeText(GeoMeasure.this, "No Results", 0);

                    	dialog.dismiss();
                    }
                });
    	    	dialog.show();
				return true;
			}
			
			case 2: // Delete Location
			{
		        final Spinner spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
    	        mDbHelper.deleteLocation(spinnerTo.getSelectedItemId());
    	    	fillData();
    	        return true;
			}
			
			case 3: // Lookup Contact
			{
        		startSubActivity(new Intent(Intent.PICK_ACTION, Contacts.People.CONTENT_URI), 0);            
    	        return true;
			}
		}
		return false;
	}

	
	// Lookup Contact returns result
    protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
    	if (requestCode == 0) {
    		if (resultCode == RESULT_OK) {
    		    Cursor cur = managedQuery(Uri.parse(data), null, null, null); 
    		    if (cur.first()) {
           			String name = cur.getString(cur.getColumnIndex(Contacts.PeopleColumns.NAME));
           			
        	    	/*Geocoder geocoder = new Geocoder();
        	        Address[] results = null;
        	        try {
        	        	results = geocoder.getFromLocationName(address, latitude, longitude, 180, 360);
        	        }
        	        catch (Exception ex) { }

        	        if (results != null && results.length > 0) {*/
        	        	mDbHelper.createLocation(name, 0, 0);//results[0].getLatitude(), results[0].getLongitude());
        	        	fillData();
        	        //}
    		    }
    		}
    	}
    }
    
    
    // Location Changed
    public class LocationIntentReceiver extends IntentReceiver{
    	@Override
    	public void onReceiveIntent(Context context, Intent intent) {
    		try {
    			//Location currentLocation = (Location)intent.getExtra("location");
    			Location currentLocation = locationManager.getCurrentLocation("gps");
    			
	    		if (toLocation == null) {
	    			compassView.update(currentLocation.getBearing(), 0);
	    		} else {
	    			setDistances(currentLocation.distanceTo(toLocation));
	    			compassView.update(currentLocation.getBearing(), currentLocation.bearingTo(toLocation));
	    		}
    		} catch (Exception exc) { }
    	}
    }
    
     
    // Update current location and distances and bearings
    public void update() {
    	Location currentLocation = locationManager.getCurrentLocation("gps");
		if (toLocation == null) {
			compassView.update(currentLocation.getBearing(), 0);
		} else {
			setDistances(currentLocation.distanceTo(toLocation));
			compassView.update(currentLocation.getBearing(), currentLocation.bearingTo(toLocation));
		}
    }
  
    
    // Set distances in GUI
    private void setDistances(double distance) {
       	TextView txtDistanceKm = (TextView) findViewById(R.id.distanceKm);
        txtDistanceKm.setText(String.format("%.2f km", distance / 1000));

        double distanceYards = distance * 1.093613;
       	
        TextView txtDistanceMiles = (TextView) findViewById(R.id.distanceMiles);
        txtDistanceMiles.setText(String.format("%.2f miles", distanceYards / 1760));
       	
        TextView txtDistanceYards = (TextView) findViewById(R.id.distanceYards);
        txtDistanceYards.setText(String.format("%.0f yards", distanceYards));
       	
        TextView txtDistanceFeet = (TextView) findViewById(R.id.distanceFeet);
        txtDistanceFeet.setText(String.format("%.0f feet", distanceYards * 3));
    }
        
    
    // Fill Spinner from DB
    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor locationsCursor = mDbHelper.fetchAllLocations();
        startManagingCursor(locationsCursor);
        
        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{LocationsDbAdapter.KEY_NAME};
        
        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.name};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter locations = new SimpleCursorAdapter(this, R.layout.locations_row, locationsCursor, from, to);

        Spinner spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
        spinnerTo.setAdapter(locations);
    }
        
    
    // onResume, onFreeze, & onDestroy
    @Override 
    public void onResume() { 
    	super.onResume(); 
		List<LocationProvider> providers = locationManager.getProviders();
		LocationProvider locationProvider = providers.get(0);
		locationManager.requestUpdates(locationProvider, 0, 0, intent);
    }
    
    @Override 
    public void onFreeze(Bundle icicle) { 
    	locationManager.removeUpdates(intent); 
    	super.onFreeze(icicle); 
    } 

    @Override 
    public void onDestroy() { 
    	unregisterReceiver(intentReceiver); 
    	super.onDestroy(); 
    } 

    
    // CompassView
    private class CompassView extends View {
        private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float myBearing;
        private float targetBearing;
        private float x = 160;
        private float y = 200;
        private float r = 80;
        final private int textSize = 20;
        final private int halfTextSize = 7;

        public CompassView(Context context) {
            super(context);
            paint.setStyle(Style.STROKE);
            paint.setTextSize(textSize);
            paint.setTextAlign(Align.CENTER);
            
            DisplayMetrics dm = new android.util.DisplayMetrics();
            WindowManager wm = getWindowManager();
            if (wm == null) return;
            android.view.Display dsp = wm.getDefaultDisplay();
            if (dsp == null) return;
            dsp.getMetrics(dm);
            x = dm.widthPixels / 2;
            r = dm.widthPixels / 3;
        }
        
        public void update(float myBearing, float targetBearing)
        {
        	this.myBearing = myBearing;
        	this.targetBearing = targetBearing;
        	invalidate();
        }
        
        @Override protected void onDraw(Canvas canvas) {
        	canvas.translate(x, y);

        	canvas.rotate(-myBearing, 0, 0);
        	paint.setARGB(255, 255, 255, 255);
            paint.setStrokeWidth(2);
            canvas.drawCircle(0, 0, r, paint);
            canvas.drawLine(-r, 0, r, 0, paint);
            canvas.drawLine(0, -r, 0, r, paint);
            canvas.drawText("N", 0, -r - 10 +halfTextSize, paint);
            canvas.drawText("S", 0, r + 10 +halfTextSize, paint);
            canvas.drawText("E", r + 10, halfTextSize, paint);
            canvas.drawText("W", -r - 10, halfTextSize, paint);

        	canvas.rotate(targetBearing, 0, 0);
            paint.setARGB(255, 255, 0, 0);
            paint.setStrokeWidth(3);
            canvas.drawLine(0, 0, 0, -r, paint);
            canvas.drawLine(0, -r, -10, -r+10, paint);
            canvas.drawLine(0, -r, 10, -r+10, paint);
        }
    }    
}