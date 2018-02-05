/*
 * 
 * For testing only. This is not part of 360 SFS
 * 
 */

package org.upesh.SFS360;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.io.InputStream;

import org.upesh.SFS360.NMEA.GPSPosition;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;

public class getGPSData {
    public static void main(String[] args) throws IOException {

    	NMEA nmea = new NMEA();
    	        Socket socket = null;  
    	        DataOutputStream os = null;
    	        DataInputStream is = null;

    	        try {
    	        	socket = new Socket("192.168.2.4", 7777);
    	            os = new DataOutputStream(socket.getOutputStream());
    	            is = new DataInputStream(socket.getInputStream());
    	        } catch (UnknownHostException e) {
    	            System.err.println("Don't know about host: hostname");
    	        } catch (IOException e) {
    	            System.err.println("Couldn't get I/O for the connection to: hostname");
    	        }

    	    if (socket != null && os != null && is != null) {
    	            try {

    	                String responseLine; // hold nmea code retrieved from GPSReceiver
    	                InputStream stream = null;
    	                GPSPosition position = null;

    	                while ((responseLine = is.readLine()) != null) {   	                	
    	                    System.out.println("Server: " + responseLine);
    	                    stream = new ByteArrayInputStream(responseLine.getBytes(StandardCharsets.UTF_8));
    	                    position =nmea.parse(responseLine);
    	                    System.out.println(position);
   	                    
    	                    if (responseLine.indexOf("Ok") != -1) {
    	                    	break;
    	                    }
    	                    if (position.lat != 0.0f){
    	                    	break;
    	                    }
    	                }
    	                os.close();
    	                is.close();
    	                socket.close();
    	                
    	                
    	                // Reverse-Geocoding
    	                //
    	        		final Geocoder geocoder = new Geocoder(); //34.223637, 72.236862
    	        		//Double l1 = 34.223920600000000000;
    	        		//Double l2 = 72.236397199999970000;

    	        		BigDecimal lat =  new BigDecimal(position.lat);;
    	        		BigDecimal lng =  new BigDecimal(position.lon);;
    	        		
    	        		LatLng location = new LatLng(lat, lng);
    	        		
     	        		GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setLocation(location).setLanguage("en").getGeocoderRequest();
    	        		
    	        		try {
    	        			GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
    	        			System.out.println(geocoderResponse.getResults());
    	        			
    	        			List<GeocoderResult> results = geocoderResponse.getResults();
    	        			for( GeocoderResult geores: results ){
    	        			    if( geores.getTypes().contains( "route" ) ){
    	        			        System.out.println( geores.getFormattedAddress() );
    	        			    }
    	        			}    	        			
    	        			
    	        		} catch (IOException e) {
    	        			// TODO Auto-generated catch block
    	        			e.printStackTrace();
    	        		}
    	        		// end of reverse Geocoding
    	                
    	                
    	            } catch (UnknownHostException e) {
    	                System.err.println("Trying to connect to unknown host: " + e);
    	            } catch (IOException e) {
    	                System.err.println("IOException:  " + e);
    	            }
    	        }
    	    
    	    
    	    
    }
    
}