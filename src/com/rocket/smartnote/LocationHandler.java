package com.rocket.smartnote;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

public class LocationHandler {
	
	private final Context ctx;
	private LocationManager locMan;
	
	public LocationHandler(Context context) {
		this.ctx = context;
		locMan = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE); 
	}
	
	public boolean gpsEnabled() {
		return locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public String getLocation() {
		double[] gps = getGPS();
		
    	Geocoder coder = new Geocoder(ctx);
    	List<Address> addresses;
    	try {
			addresses = coder.getFromLocation(gps[0], gps[1], 1);
		} catch (IOException e) {
			return "Unknow";
		}
    	
    	if (addresses != null && addresses.size() > 0) {
    		return addresses.get(0).getAddressLine(0);
    	}
    	return "Unknow";
     }
    
    /**
     * Get current GPS data
     * @return
     */
    private double[] getGPS() {
    	double[] gps = new double[2];
    	
    	if (!gpsEnabled()) {
    		return gps;
    	}
    	
    	List<String> providers = locMan.getProviders(true);

    	/* Loop over the array backwards, and if you get an accurate location, then break                 out the loop*/
    	Location l = null;

    	for (int i=providers.size()-1; i>=0; i--) {
    		l = locMan.getLastKnownLocation(providers.get(i));
    		if (l != null) break;
    	}
  	
    	if (l != null) {
    		gps[0] = l.getLatitude();
    		gps[1] = l.getLongitude();
    		System.out.println(l.toString());
    	}
    	else {
    		System.out.println("location is null");
    	}
    	
    	System.out.println("GPS: " + gps[0] + " " + gps[1]);
    	return gps;
    }  
}
