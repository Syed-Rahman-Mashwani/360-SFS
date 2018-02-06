/*
 * 
 * 360 Semantic File System.
 * 
 * Core360.java is main class of 360-SFS containing various methods which are called from 
 * 360-SFS main program (SFS_360.java) on different File System operations.
 * 
 * 
 */

package org.upesh.SFS360;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.query.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.upesh.SFS360.NMEA.GPSPosition;

public class Core360 
{
	static final String inputFileName  = "./target/resources/sfs_Ontology.owl";
	static final String baseURI = "http://www.semanticweb.org/administrator/ontologies/2014/2/untitled-ontology-5";
	String currentLocation= "";
	
	public Core360(Boolean setLoc)  //constructor. 
	{
		if (setLoc) //set location at startup via input dialog box, if the argument passed is true.
		{
			setCurrentLocation();
		}
		else // get coordinates from GPS receiver and reverse-geocode it. If in case any error occurs in connectivity with GPS receiver or in reverse-geociding then pop up input dialog box for manually entering location.
		{
	    	NMEA nmea = new NMEA(); //creating instance of NMEA to parse NMEA code received from GPS Receiver
	        Socket socket = null;  
	        DataOutputStream os = null;
	        DataInputStream is = null;

	        try {
	        	socket = new Socket("192.168.2.4", 7777); 
	            os = new DataOutputStream(socket.getOutputStream());
	            is = new DataInputStream(socket.getInputStream());
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host: host \n...\nSet location manually via input dialog box... ");
	            setCurrentLocation();
	            
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to: host \n...\nSet location manually via input dialog box...");
	            setCurrentLocation();
	        }

	        if (socket != null && os != null && is != null) {
	            try {

	                String responseLine; // hold nmea code retrieved from GPSReceiver
	                GPSPosition position = null;

	                while ((responseLine = is.readLine()) != null) {   	                	
	                    System.out.println("Server: " + responseLine);
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
	                System.out.println("Reverse Geocoding lat:"+position.lat+ "; lon: "+position.lon+" ...");
	                System.out.println("...");
	                
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
	        			        setCurrentLocation(geores.getFormattedAddress()); //setting current location of the user 
	        			    }
	        			}    	        			
	        			
	        		} catch (IOException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        			System.err.println("Reverse geocoding unsuccessful  \n...\nSet location manually via input dialog box... ");
	        			setCurrentLocation();
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

	public String getPathOfIndividual(String _URI) // get URI and return path of resource/individual
	{
		String path1 = null;
		
		
		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }
		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// model.write(System.out);
		 String hasPath = "<"+baseURI+"#hasPath"+">";
		 String queryString = 
		"SELECT ?z WHERE { <"+_URI+">" + hasPath + "?z }";
		 Query query = QueryFactory.create(queryString);

		 // Execute the query and obtain results
		 QueryExecution qe = QueryExecutionFactory.create(query, model);
		 try { 
				
			 ResultSet results = qe.execSelect();
			 for ( ; results.hasNext() ; )
			    {
			      QuerySolution soln = results.nextSolution() ;			    
			      path1 = soln.getLiteral("z").toString();
			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
			
		return path1;				
		
	}	

	public String getURIOfIndividual(String path1) // Gets file's/folder's path. And return its URI_UUID as string if individual of file/folder having path exist. If not exist then return null
	{ 
	
		String URI_UUID=null;
		path1 = getStringInQuotes(path1);

		 
		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }
		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// model.write(System.out);
		 String hasPath = "<"+baseURI+"#hasPath"+">";
		 String queryString = 
		"SELECT ?x WHERE { ?x "+hasPath + path1 +"}";
		 Query query = QueryFactory.create(queryString);

		 // Execute the query and obtain results
		 QueryExecution qe = QueryExecutionFactory.create(query, model);
		 try { 
				
			 ResultSet results = qe.execSelect();
			 for ( ; results.hasNext() ; )
			    {
			      QuerySolution soln = results.nextSolution() ;
			    
			      Resource r = soln.getResource("x");
			      URI_UUID = r.toString();
			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
		 

		return URI_UUID;			
				
	}
	
	public List<String> getURIOfIndividual(String propertyName, String literal, Boolean distinct) // Gets property and its literal value. And return list of URI_UUIDs as List<string>. If not exist then return null
	{ 	
		String strDistinct = "";
		if (distinct == true)
			strDistinct="DISTINCT";
		
		List<String> URI_UUID = new ArrayList<String>();
		literal = getStringInQuotes(literal);
		 
		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }
		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// model.write(System.out);
		 //String hasPath = "<"+baseURI+"#hasPath"+">";
		 propertyName = "<"+baseURI+propertyName+">";
		 String queryString = 
		"SELECT "+strDistinct+"?x WHERE { ?x "+propertyName + literal +"}";
		 Query query = QueryFactory.create(queryString);

		 // Execute the query and obtain results
		 QueryExecution qe = QueryExecutionFactory.create(query, model);
		 try { 
				
			 ResultSet results = qe.execSelect();
			 for ( ; results.hasNext() ; )
			    {
			      QuerySolution soln = results.nextSolution() ;
			    
			      Resource r = soln.getResource("x");
			      URI_UUID.add(r.toString());
			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
		 

		return URI_UUID;			
				
	}
	
	public List<String> getURIsOfTemporallyPeakFiles(String peakDay, String peakHour) // Gets gets current Day and Hour and return URI of those individuals whose peak Hour and Day are the arguments passed to this function. Return value is List<string>. If not exist then return null
	{ 	
		 
		List<String> URI_UUID = new ArrayList<String>();
		peakHour = getStringInQuotes(peakHour);
		peakDay = getStringInQuotes(peakDay);
		 
		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }
		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// model.write(System.out);
		 //String hasPath = "<"+baseURI+"#hasPath"+">";
		 String property1Name = "<"+baseURI+"#filePeakHour"+">";
		 String property2Name = "<"+baseURI+"#filePeakDay"+">";
		 String queryString = 
		"PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#> SELECT ?x WHERE { ?x "+property1Name + peakHour +"^^xsd:int; "+ property2Name + peakDay +"^^xsd:int }";
		 Query query = QueryFactory.create(queryString);

		 // Execute the query and obtain results
		 QueryExecution qe = QueryExecutionFactory.create(query, model);
		 try { 
				
			 ResultSet results = qe.execSelect();
			 for ( ; results.hasNext() ; )
			    {
			      QuerySolution soln = results.nextSolution() ;
			    
			      Resource r = soln.getResource("x");
			      System.out.println("resource="+r);
			      URI_UUID.add(r.toString());
			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
		 

		return URI_UUID;					
				
	}
	
	
	public String getStringInQuotes(String p1) // enclose string in double quotes
	{
		return "\""+p1+"\"";		
	}

	public String getRandomUUID()
	{
	return UUID.randomUUID().toString();	
	}

	public String createIndividual(String ofType) //create individual of a class 
	{
		Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 Resource resource = model.createResource(baseURI+"#"+getRandomUUID());
		 Resource resourceOfType= model.createResource(baseURI+ofType);
		 
		 resource.addProperty(RDF.type, resourceOfType);
		 
			try {
				FileWriter out = new FileWriter( inputFileName );
				model.write( out, "RDF/XML-ABBREV" );
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		return resource.toString();
	}

	public void setDataTypeProperty(String resourceURI, String propertyName, int propertyValue) //create new data type property. 
	{
		if (resourceURI==null)
		return;
		
		Model model = ModelFactory.createDefaultModel();
		
		//read model from file
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		 // Add property to Model
		 Resource resource = model.createResource(resourceURI);
		 resource.addProperty(model.createProperty(baseURI+propertyName), model.createTypedLiteral(propertyValue));

		 
		 //Writing model to file
			try {
				FileWriter out = new FileWriter( inputFileName );
				model.write( out, "RDF/XML-ABBREV" );
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	public void setDataTypeProperty(String resourceURI, String propertyName, Date propertyValue) //create new data type property
	{
		if (resourceURI==null)
		return;
    	
		Calendar cal = Calendar.getInstance();
		cal.setTime(propertyValue);
		
		Model model = ModelFactory.createDefaultModel();
		
		//read model from file
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		// Add property to Model
		Resource resource = model.createResource(resourceURI);
		resource.addProperty(model.createProperty(baseURI+propertyName), model.createTypedLiteral(cal));

		 
		//Writing model to file
			try {
				FileWriter out = new FileWriter( inputFileName );
				model.write( out, "RDF/XML-ABBREV" );
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	public void setDataTypeProperty(String resourceURI, String propertyName, String propertyValue) //create new data type property
	{
		if (resourceURI==null)
		return;
		
		Model model = ModelFactory.createDefaultModel();
		
		//read model from file
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		 // Add property to Model
		 Resource resource = model.createResource(resourceURI);
		 resource.addProperty(model.createProperty(baseURI+propertyName), model.createLiteral(propertyValue));

		 
		 //Writing model to file
			try {
				FileWriter out = new FileWriter( inputFileName );
				model.write( out, "RDF/XML-ABBREV" );
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	public List<String> getDataTypeProperty (String subjectURI, String propertyName) //gets URI of subject and property Name, and return arrayList of strings/values/resources 
	{
		String properValue = null;
		List<String> list = new ArrayList<String>();
		//String subjectURI = "http://www.semanticweb.org/administrator/ontologies/2014/2/untitled-ontology-5#a1bec841-af15-4ab1-8377-8cfb0e440240";		
		//String propertyName =	"#fileLastAccessed";	
		

		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }
		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// model.write(System.out);
		 
		 String hasPath = "<"+baseURI+propertyName+">";
		 String queryString = 
		"SELECT ?z WHERE { <"+subjectURI+">" + hasPath + "?z }";
		 Query query = QueryFactory.create(queryString);

		 
		 // Execute the query and obtain results
		 QueryExecution qe = QueryExecutionFactory.create(query, model);
		 try { 
				
			 ResultSet results = qe.execSelect();
			 
			 for ( ; results.hasNext() ; )
			    {
			      QuerySolution soln = results.nextSolution() ;
			    
			      properValue = soln.getLiteral("z").toString();
			      System.out.println( properValue );
			      list.add(properValue);

			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
			
		//printing list	
		/*System.out.println("Printing list");
		for (int i = 0; i < list.size(); i++)
		{
			System.out.println(list.get(i));
		}
		*/
		 
		return list;

	}

	public List<String> getDataTypeProperty (String propertyName, Boolean distinct) //property Name, and return arrayList of strings/values/resources 
	{
		String strDistinct = "";
		if (distinct == true)
			strDistinct="DISTINCT";
		
		String properValue = null;
		List<String> list = new ArrayList<String>();
		//String subjectURI = "http://www.semanticweb.org/administrator/ontologies/2014/2/untitled-ontology-5#a1bec841-af15-4ab1-8377-8cfb0e440240";		
		//String propertyName =	"#fileLastAccessed";	
		

		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }
		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// model.write(System.out);
		 
		 String hasPath = "<"+baseURI+propertyName+">";
		 String queryString = 
		"SELECT "+strDistinct+"?z WHERE {  ?x" + hasPath + "?z }";
		 Query query = QueryFactory.create(queryString);

		 
		 // Execute the query and obtain results
		 QueryExecution qe = QueryExecutionFactory.create(query, model);
		 try { 
				
			 ResultSet results = qe.execSelect();
			 
			 for ( ; results.hasNext() ; )
			    {
			      QuerySolution soln = results.nextSolution() ;			    
			      properValue = soln.getLiteral("z").toString();
			      list.add(properValue);
			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
			
		//printing list	
		/*System.out.println("Printing list");
		for (int i = 0; i < list.size(); i++)
		{
			System.out.println(list.get(i));
		}
		*/
		 
		return list;

	}
	
	public void updateDataTypeProperty(String resourceURI, String propertyName, String propertyOldValue, String propertyNewValue)//This method deletes the statement and then create new one
	{
		if (resourceURI==null)
			return;
		
		Model model = ModelFactory.createDefaultModel();
		
		//read model from file
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 // delete the statement having old property
		 Resource resource = model.createResource(resourceURI);
		 model.remove(resource, model.createProperty(baseURI+propertyName), model.createLiteral(propertyOldValue));
		 
		 // Add new statement with new property to Model
		 resource.addProperty(model.createProperty(baseURI+propertyName), model.createLiteral(propertyNewValue));

		 
		 //Writing model to file
			try {
				FileWriter out = new FileWriter( inputFileName );
				model.write( out, "RDF/XML-ABBREV" );
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}

	public void deleteDataTypeProperty(String resourceURI, String propertyName, String propertyValue)//This method deletes the statement 
	{
		if (resourceURI==null)
			return;
		
		Model model = ModelFactory.createDefaultModel();
		
		//read model from file
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 // delete the statement having old property
		 Resource resource = model.createResource(resourceURI);
		 model.remove(resource, model.createProperty(baseURI+propertyName), model.createLiteral(propertyValue));
		 
		 //Writing model to file
			try {
				FileWriter out = new FileWriter( inputFileName );
				model.write( out, "RDF/XML-ABBREV" );
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	public void deleteDataTypeProperty(String propertyName, String propertyValue)//This method deletes the statement(s) 
	{
		
		Model model = ModelFactory.createDefaultModel();
		
		//read model from file
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
		List<String> resourceURI= new ArrayList<String>();
		resourceURI = getURIOfIndividual(propertyName, propertyValue, true);
		
		for (int i=0; i<resourceURI.size(); i++)
		{
			// delete the statement having old property
			 Resource resource = model.createResource(resourceURI.get(i));
			 model.remove(resource, model.createProperty(baseURI+propertyName), model.createLiteral(propertyValue));			 
		}

		 //Writing model to file
		try {
				FileWriter out = new FileWriter( inputFileName );
				model.write( out, "RDF/XML-ABBREV" );
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	}
	
	public List<String> getFilePath (String subjectURI, String propertyName) //gets URI of subject and property Name, and return arrayList of strings/values/resources 
	{
		String properValue = null;
		List<String> list = new ArrayList<String>();
		//String subjectURI = "http://www.semanticweb.org/administrator/ontologies/2014/2/untitled-ontology-5#a1bec841-af15-4ab1-8377-8cfb0e440240";		
		//String propertyName =	"#fileLastAccessed";	
		

		Model model = ModelFactory.createMemModelMaker().createDefaultModel();
		//Model model = ModelFactory.createDefaultModel();
		
		InputStream in = FileManager.get().open(inputFileName);
		
		 if (in == null) 
		 {
			 throw new IllegalArgumentException( "File: " + inputFileName + " not found");
	     }
		 
		 model.read(in, "");
		 try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// model.write(System.out);
		 
		 String hasPath = "<"+baseURI+propertyName+">";
		 String queryString = 
		"SELECT ?z WHERE { <"+subjectURI+">" + hasPath + "?z }";
		 Query query = QueryFactory.create(queryString);

		 
		 // Execute the query and obtain results
		 QueryExecution qe = QueryExecutionFactory.create(query, model);
		 try { 
				
			 ResultSet results = qe.execSelect();
			 
			 for ( ; results.hasNext() ; )
			    {
			      QuerySolution soln = results.nextSolution() ;
			    
			      properValue = soln.getLiteral("z").toString();
			      System.out.println( properValue );
			      list.add(properValue);

			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
			
		 
		return list;

	}

	
	public void handleNameQuery(String resourceURI, String path1, String queryPortion) //if path contains separate query from name(path) and annotate the object accordingly 
	{
		
		
		//Separating query type an values from  query portion of name
		String q1[] = queryPortion.split(":",2);
		String queryType = q1[0].toString();
		String queryValues = q1[1].toString().toLowerCase();
		
		System.out.println("Query Type:"+queryType);
		System.out.println("Query Value:"+queryValues);	
		
		
		// if name contains setTag keyword then apply the specified tag on the object
		if (queryType.equalsIgnoreCase("setTag"))
		{		
			System.out.println("Setting "+queryValues+" Tag on "+path1+" ...");
			try	{
				
				setDataTypeProperty(resourceURI, "#hasTag", queryValues.trim());
			}
			catch (Exception e)
			{e.printStackTrace();}
			
		}
		
	}
	

	
	@SuppressWarnings("unchecked")
	public void locationDaemon(String resourceURI )
	{
		if (resourceURI==null)
			return;
		
		List<String> listOfFileAccessedInAllLocations = new ArrayList<String>();
		List<Integer> daysList = new ArrayList<Integer>();
		List<Integer> hoursList = new ArrayList<Integer>();
		
		
		// Deleting all old entries of Peak location
		List<String> oldPeakLocation = new ArrayList<String>();
		
		oldPeakLocation = getDataTypeProperty(resourceURI, "#filePeakLocation");
		
		for (int i = 0; i < oldPeakLocation.size(); i++)
		{
			deleteDataTypeProperty(resourceURI, "#filePeakLocation",oldPeakLocation.get(i));
		}

		//calculating new peak location
		//get all locations where file is accessed
		List<String> lst = new ArrayList<String>();
		lst = getDataTypeProperty(resourceURI, "#fileAccessedLocation");
		String s[] =null;
		for (int i = 0; i < lst.size(); i++)
		{
			s=lst.get(i).split("_atTime_");
			listOfFileAccessedInAllLocations.add(s[0]);				
		}
		
		// Peak locations calculation
		System.out.println("\nPeak locations calculation");	
		
		Set<String> uniqueLocations = new HashSet<String>(listOfFileAccessedInAllLocations);
		System.out.println("Unique locations count: " + uniqueLocations.size());
		
		double t1 = listOfFileAccessedInAllLocations.size()/(double)uniqueLocations.size(); //Threshold = total hits of a file in all location divide by total number of distinct location
			
		for (int i = 0; i < listOfFileAccessedInAllLocations.size(); i++)
		{
			System.out.println(listOfFileAccessedInAllLocations.get(i));
		}
		
		System.out.println(" \n (total number of hits in all locations)= "+ listOfFileAccessedInAllLocations.size() +"\n t (treshold value)= "+ listOfFileAccessedInAllLocations.size() +"/ "+uniqueLocations.size()+" ="+t1);
		
		//calculating frequency of each location, if frequency exceed threshold then add to peak location

		List<String> uniqueListofLocations = new ArrayList<String>(uniqueLocations);
		
		for (int i =0; i < uniqueListofLocations.size(); i++)
		{
			int freq = Collections.frequency(listOfFileAccessedInAllLocations, uniqueListofLocations.get(i));
			System.out.println("frequency of "+uniqueListofLocations.get(i)+"="+freq);

			if (freq > 0 && freq >= t1)
			{
				System.out.println("Location "+uniqueListofLocations.get(i)+" is added as peak location");
				setDataTypeProperty(resourceURI, "#filePeakLocation",uniqueListofLocations.get(i));				
			}
		}
		

	}
	
	public void temporalDaemon(String resourceURI )
	{
		if (resourceURI==null)
			return;
		
		List<String> listOfFileAccessed = new ArrayList<String>();
		List<Integer> daysList = new ArrayList<Integer>();
		List<Integer> hoursList = new ArrayList<Integer>();
		
		
		// Deleting all old entries of Peak day and Peak Hours
		List<String> oldPeakDays = new ArrayList<String>();
		List<String> oldPeakHours = new ArrayList<String>();
		
		oldPeakDays = getDataTypeProperty(resourceURI, "#filePeakDay");
		oldPeakHours = getDataTypeProperty(resourceURI, "#filePeakHour");
		
		for (int i = 0; i < oldPeakDays.size(); i++)
		{
			deleteDataTypeProperty(resourceURI, "#filePeakDay",oldPeakDays.get(i));
		}
		for (int i = 0; i < oldPeakHours.size(); i++)
		{
			deleteDataTypeProperty(resourceURI, "#filePeakHour",oldPeakHours.get(i));
		}

		
		//get values of fileLastAccessed dataTypeProterty
		listOfFileAccessed = getDataTypeProperty(resourceURI, "#fileLastAccessed");
		
		//separating days and hours in new lists from date list
		for (int i = 0; i < listOfFileAccessed.size(); i++)
		{
			
			try {
				//Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).parse(listOfFileAccessed.get(i));
		       	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		    	sdf.setTimeZone(TimeZone.getTimeZone(""));
		    	Date date = sdf.parse(listOfFileAccessed.get(i));
				daysList.add(date.getDay());
				hoursList.add(date.getHours());
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
		}
				
		// Peak days calculation
		double t1 = daysList.size()/7.0;
		System.out.println("\nDays");		
		for (int i = 0; i < daysList.size(); i++)
		{
			System.out.println(daysList.get(i));
		}
		
		System.out.println(" n (total number of hits)= "+ daysList.size() +"\n t (treshold value)= "+ daysList.size() +"/7 = "+t1);
		
		for (int i =0; i <= 6; i++)
		{
			int freq = Collections.frequency(daysList, i);
			System.out.println("frequency of "+i+"="+freq);

			if (freq > 0 && freq >= t1)
			{
				System.out.println("day "+i+" is added as peak day");
				setDataTypeProperty(resourceURI, "#filePeakDay",i);				
			}
		}

		
		// Peak hours calculation
		System.out.println("\n\nHours: "); 
		double t2 = hoursList.size()/24.0;
		
		for (int i = 0; i < hoursList.size(); i++)
		{
			System.out.println(hoursList.get(i));
		}
		
		System.out.println(" n (total number of hits)= "+ hoursList.size() +"\n t (treshold value)= "+ hoursList.size() +"/24 = "+t2);
		
		for (int i =0; i <= 23; i++) //0 is Sunday
		{
			int freq = Collections.frequency(hoursList, i);
			System.out.println("frequency of "+i+"="+freq);

			if (freq > 0 && freq >= t2)
			{
				System.out.println("hour "+i+" is added as peak hour");
				setDataTypeProperty(resourceURI, "#filePeakHour",i);
			}

		}

	}

	
	public void setCurrentLocation(){ //set current location via input dialog box
		JFrame myFrame = null;

		int messageType = JOptionPane.INFORMATION_MESSAGE;
	      currentLocation = JOptionPane.showInputDialog(myFrame, 
	         "What is your current location?", 
	         "Input Dialog Box", messageType).toLowerCase();
	      if (currentLocation == null) //handling if user click close or cancel buttons.
	    	  setCurrentLocation();
	      else
	    	  System.out.println("Current Location has been set as: "+currentLocation);
	 
	}
	public void setCurrentLocation(String currentLocation1){ //set the passed argument as current location
		currentLocation= currentLocation1;		
		System.out.println("Current Location has been set as: "+currentLocation);	 
	}
	
	public String[] splitFileNameIntoBaseAndExtension(String fileName) // gets file name. Separate base and extension from file name. tokens[0] is base and tokens[1] is extension 
	{
		String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
		return tokens;
		//tokens[0] is base and tokens[1] is extension
	}

}
