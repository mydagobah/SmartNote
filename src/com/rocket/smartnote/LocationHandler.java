package com.rocket.smartnote;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationHandler {
	
	private final Context ctx;
	private LocationManager locMan;
	private String latest = "Unknow";
	
    private static final int ONE_SECONDS = 1000;
    private static final int ONE_METERS = 1;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	public LocationHandler(Context context) {
		this.ctx = context;
		locMan = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE); 
	}
	
	/**
	 * @return - the latest location
	 */
	public String getLocation() {
		return latest;
    }
		
	public boolean gpsEnabled() {
		return locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public void captureLocation() {		
		Location gpsLocation = null;
        Location networkLocation = null;
        
        System.out.println("in captureLocaiton");
        //locMan.removeUpdates(listener);
                
        // Request updates from both fine (gps) and coarse (network) providers.
        gpsLocation = requestUpdatesFromProvider(
                LocationManager.GPS_PROVIDER, R.string.not_support_gps);
        networkLocation = requestUpdatesFromProvider(
                LocationManager.NETWORK_PROVIDER, R.string.not_support_network);

        // If both providers return last known locations, compare the two and use the better
        // one to update the UI.  If only one provider returns a location, use it.
        if (gpsLocation != null && networkLocation != null) {
            updateLocation(getBetterLocation(gpsLocation, networkLocation));
        } else if (gpsLocation != null) {
            updateLocation(gpsLocation);
        } else if (networkLocation != null) {
            updateLocation(networkLocation);
        }
	}
	
	private void updateLocation(Location location) {	        
    	// Since the geocoding API is synchronous and may take a while.  You don't want to lock
        // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
        //(new ReverseGeocodingTask(ctx)).execute(new Location[] {location});
		Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());

        Location loc = location;
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) {
            latest = "Location Error";
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            // Format the first line of address (if available), city, and country name.
            latest = String.format("%s, %s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getLocality(),
                    address.getCountryName());                
        }
        else {
        	latest = "fail";
        }
    }
	private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
        Location location = null;
       
        if (locMan.isProviderEnabled(provider)) {
            locMan.requestLocationUpdates(provider, ONE_SECONDS, ONE_METERS, listener);
            location = locMan.getLastKnownLocation(provider);
        }
        return location;
    }
	private final LocationListener listener = new LocationListener() {	
        @Override
        public void onLocationChanged(Location location) {
            // A new location update is received.  Do something useful with it.  Update the UI with
            // the location update.
            updateLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        	latest = provider + " disabled!";
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }	    
	};
		    	
	 /** Determines whether one Location reading is better than the current Location fix.
      * Code taken from
      * http://developer.android.com/guide/topics/location/obtaining-user-location.html
      *
      * @param newLocation  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new
      *        one
      * @return The better Location object based on recency and accuracy.
      */
    protected Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return newLocation;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return newLocation;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }
    
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
	    
}
