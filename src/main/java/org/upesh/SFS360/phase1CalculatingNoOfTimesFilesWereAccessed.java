package org.upesh.SFS360;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class phase1CalculatingNoOfTimesFilesWereAccessed {

	static final String inputFileName  = "./target/resources/iftihar.owl";
	static final String baseURI = "http://www.semanticweb.org/administrator/ontologies/2014/2/untitled-ontology-5";
	

	public static void main(String[] args) {
		
	    	  
		
		totalNumberOfTimeFileWereAccessed("#fileLastAccessed");
		totalNumberOfUniqueFileWereAccessed("#fileLastAccessed");
		System.out.println("Names of unique files accessed: ");
		NamesOfUniqueFileWereAccessed("#fileLastAccessed");
		System.out.println("Paths of unique files accessed: ");
		pathOfUniqueFileWereAccessed("#fileLastAccessed");
		
		
	}

	public static List<String> totalNumberOfTimeFileWereAccessed(String propertyName) // Gets gets property and its literal value. And return list of URI_UUIDs as List<string>. If not exist then return null
	{ 	
		List<String> URI_UUID = new ArrayList<String>();

		 
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
		"SELECT ?x WHERE { ?x "+propertyName +" ?y}";
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
		 
		System.out.println("Number of times files were accessed: "+URI_UUID.size());
		return URI_UUID;			
				
	}
	
	public static List<String> totalNumberOfUniqueFileWereAccessed(String propertyName) // Gets gets property and its literal value. And return list of URI_UUIDs as List<string>. If not exist then return null
	{ 	
		List<String> URI_UUID = new ArrayList<String>();

		 
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
		"SELECT DISTINCT ?x WHERE { ?x "+propertyName +" ?y}";
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
		 
		System.out.println("Number of unique files accessed: "+URI_UUID.size());
		return URI_UUID;			
				
	}
	
	
	public static List<String> pathOfUniqueFileWereAccessed(String propertyName) // Gets gets property and its literal value. And return list of URI_UUIDs as List<string>. If not exist then return null
	{ 	
		List<String> URI_UUID = new ArrayList<String>();

		 
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
		"SELECT DISTINCT ?x WHERE { ?x "+propertyName +" ?y}";
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
			      System.out.println(getPathOfIndividual(r.toString()));
			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
		 
		return URI_UUID;			
				
	}

	public static List<String> NamesOfUniqueFileWereAccessed(String propertyName) // Gets gets property and its literal value. And return list of URI_UUIDs as List<string>. If not exist then return null
	{ 	
		List<String> URI_UUID = new ArrayList<String>();

		 
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
		"SELECT DISTINCT ?x WHERE { ?x "+propertyName +" ?y}";
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
			      File f = new File(getPathOfIndividual(r.toString()));
			      System.out.println(f.getName());

			    }
			 qe.close();
			 
		 } catch (Exception ex){
			 
		 }
		 
		return URI_UUID;			
				
	}
	
	public static String getPathOfIndividual(String _URI) // get URI and return path of resource/individual
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
}