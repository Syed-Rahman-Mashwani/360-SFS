/*
 * 
 * For testing only. This is not part of 360 SFS
 * 
 */

package org.upesh.SFS360;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.upesh.SFS360.NMEA.GPSPosition;

import net.fusejna.types.TypeMode.NodeType;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class test {

	static final String inputFileName  = "./target/resources/test.owl";
	static final String baseURI = "http://www.semanticweb.org/administrator/ontologies/2014/2/untitled-ontology-5";
	

	public static void main(String[] args) {
		//Core360 core360 = new Core360(false);
		
		
		final Geocoder geocoder = new Geocoder(); //34.223637, 72.236862
		Double l1 = 34.223920600000000000;
		Double l2 = 72.236397199999970000;
		//Double l1 = 34.010920600000000000;
		//Double l2 = 71.480397199999970000;
		BigDecimal lat =  new BigDecimal(l1, MathContext.DECIMAL64);;
		BigDecimal lng =  new BigDecimal(l2, MathContext.DECIMAL64);;
		
		LatLng location = new LatLng(lat, lng);
		
		//GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress("Paris, France").setLanguage("en").getGeocoderRequest();
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
		
		/*
		String s ="asdfaasgsdf";
		String[] s1 = s.split("\\.(?=[^\\.]+$)");
		System.out.println("S0="+s1[0]+s1.length);
		if(s1.length>1)
		System.out.println("S1="+s1[1]);
		else{
			s1[1] = "";
		    System.out.println("S1="+s1[1]);}
		
		
       	SimpleDateFormat sd1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	sd1.setTimeZone(TimeZone.getTimeZone(""));
    	List<String> dlist = new ArrayList<String>();
		dlist= core360.getDataTypeProperty("http://www.semanticweb.org/administrator/ontologies/2014/2/untitled-ontology-5#0cfe0d1c-c58e-4465-bc6b-e6a568112aca", "#created");
		System.out.println(dlist.get(0));
		try {
			System.out.println(sd1.parse(dlist.get(0)));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String s = dlist.get(0);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
			Date date1 = sd.parse(s);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date1);
			System.out.println(cal);
			System.out.println(cal.getTime()+" "+cal.getTimeZone());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
*/
	}
}