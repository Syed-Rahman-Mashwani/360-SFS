/*
 * 
 * 360 Semantic File System. 
 * 
 * SFS_360.java is main program.  
 * 
 * 
 */

package org.upesh.SFS360;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FlockCommand;
import net.fusejna.FuseException;
import net.fusejna.StructFlock.FlockWrapper;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

public class SFS_360 extends FuseFilesystemAdapterFull
{	
	static File f_mountPoint = new File ("./target/360_SFS");
	private final static String mountPoint = f_mountPoint.getAbsolutePath(); //absolute path of mountPoint
	private final String nowFolder = "/1-NOW";
	private final String tagsFolder = "/2-TAGs";
	private final String mirroredFolder = "./target/mirrorredFolder";
	private Date datePreviousFileAccessed = new Date();
	Map<String, String> mapSymLink = new HashMap<String, String>(); //hold path of symLink and Target path of symLink, it helps in readLink() function
	
	Boolean l= false;//set true to set location at startup via input dialog box, or false to set the current location using Smartphone's GPS Receiver
	Core360 core360 = new Core360(l);
	
	
	public static void main(String args[]) throws FuseException
	{		
		new SFS_360().log(true).mount(mountPoint); //the same mount path should be saved in mountPoint variable as absolute path
	}


	
	@Override
	public int getattr(final String path, final StatWrapper stat)
	{
		
		if (path.contains(">>"))
		{
			stat.setMode(NodeType.SYMBOLIC_LINK);
			return 0;
		}
		
		//if current path is of NOW folder
		if (path.contains(nowFolder))
		{
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}
		
		//if current path is of TAGs folder
		if (path.endsWith(tagsFolder))
		{
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}
		
		if (path.contains("TAG_"))
		{
			String[] s = path.split("/");
			if (s[s.length-1].contains("TAG_"))
			{
				stat.setMode(NodeType.DIRECTORY);
				return 0;
			}
		}

		//if current path is of users's assigned TAG 
		if (path.startsWith("TAG_"))
		{
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}
			
		
		File f = new File(mirroredFolder+path);
			
		
		//if current path is of file
		if (f.isFile())
		{
			stat.setMode(NodeType.FILE,true,true,true,true,true,true,true,true,true);
			stat.size(f.length());
			stat.atime(f.lastModified()/ 1000L);
			stat.mtime(0);
		    stat.nlink(1);
		    stat.uid(0);
		    stat.gid(0);
			stat.blocks((int) ((f.length() + 511L) / 512L));
			return 0;
		}
		
		
		//if current file is of Directory
		else if(f.isDirectory())
		{
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}
		

				
		return -ErrorCodes.ENOENT();
	}


	@Override
	public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info)
	{
		/*
		// Compute substring that we are being asked to read
		//System.out.println("read called: Path="+path);
		String contentOfFile=null;
		try {
			contentOfFile= readFile(mirroredFolder+path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String s = contentOfFile.substring((int) offset,
				(int) Math.max(offset, Math.min(contentOfFile.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		return s.getBytes().length;
		*/
		
		////////////////////
		

		Path p = Paths.get(mirroredFolder+path);
		try {
			byte[] data = Files.readAllBytes(p);	
			System.out.println("offset= "+offset+" size= "+size);
			System.out.println("Capasity of buffer is: "+buffer.capacity()+". Bytes lenth: "+data.length);

			//buffer.put(ByteBuffer.wrap(data));
			
			if ((offset + size) > data.length)
			{
				buffer.put(data, (int) offset, data.length-(int) offset);
				return (int) size;
			}
			else
			{
				buffer.put(data, (int) offset, (int) size);
				return (int) size;
			}													
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return 0;

		
		/*
		String str = "";
		try {
			File fileDir = new File(mirroredFolder+path);
	 
			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
	                      new FileInputStream(fileDir), "UTF8"));
	 			
	 
			while ((str = in.readLine()) != null) {
			    System.out.println(str);
			}
	 
	                in.close();
		    } 
		    catch (UnsupportedEncodingException e) 
		    {
				System.out.println(e.getMessage());
		    } 
		    catch (IOException e) 
		    {
				System.out.println(e.getMessage());
		    }
		    catch (Exception e)
		    {
				System.out.println(e.getMessage());
		    }
		buffer.put(str.getBytes());
		return str.getBytes().length; 
		*/

	}

	public int write(final String path, final ByteBuffer buf, final long bufSize, final long writeOffset,
			final FileInfoWrapper wrapper)
	{
		byte[] b = new byte[(int) bufSize];
		buf.get(b);
		
		
		try {			
			FileOutputStream output = new FileOutputStream(mirroredFolder+path, true);
			output.write(b); 
			output.close();
			return (int) bufSize;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		/*
		byte[] b = new byte[(int) bufSize];
		buf.get(b);
		FileOutputStream fos;
		System.out.println("writeOffset= "+writeOffset+" bufsize= "+bufSize);
		try {
			fos = new FileOutputStream(mirroredFolder+path);
		fos.write(b, 0, (int) bufSize);
		fos.close();
		return (int) bufSize;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		/*
		byte[] b = new byte[(int) bufSize];
		buf.get(b);
		RandomAccessFile file;
		
		System.out.println("writeOffset= "+writeOffset+" bufsize= "+bufSize);
		
		try {
			file = new RandomAccessFile(mirroredFolder+path, "rw");
		file.write(b, 0, (int) bufSize);

		file.close();
		return (int) bufSize;
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		
		/*
		System.out.println("Write called: path is "+path);
		
		Path p = Paths.get(mirroredFolder+path);
		try {
			byte[] data = Files.readAllBytes(p);	
			System.out.println("writeOffset= "+writeOffset+" bufsize= "+bufSize);
			System.out.println("Capasity of buffer is: "+buf.capacity()+". Bytes lenth: "+data.length);

			//buffer.put(ByteBuffer.wrap(data));
			
			if ((writeOffset + bufSize) > data.length)
			{
				buf.put(data, (int) writeOffset, data.length-(int) writeOffset);
				return (int) bufSize;
			}
			else
			{
				buf.put(data, (int) writeOffset, (int) bufSize);
				return (int) bufSize;	
			}													
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	
		
		return -1; 
			

	}
	
	@Override
	public int opendir(final String path, final FileInfoWrapper info)
	{
		//System.out.println("UUID URI of path "+path+" is : "+core360.getURIOfIndividual(path)); //prints UUID_URI of current directory

		
		//Check the individual/URI of the current folder if not yet created then create it(this in case if folders are copied directly to mirrorFolder)
		File f = new File(path);
		if (!(path.contains(nowFolder) || path.contains(tagsFolder)))
		{
			if (core360.getURIOfIndividual(path) == null && !f.isHidden())
			{				
				//create an individual of Folder class / of type Folder
				String uri = core360.createIndividual("#Folder");
				System.out.println("individual created, resouse = "+uri);
								
				//set properties of newly created individual of folder 
				System.out.println("Setting hasPath property .. ");
				core360.setDataTypeProperty(uri,"#hasPath", path);			
			}
		}
		
		Date date = new Date();
		datePreviousFileAccessed = date;
		return 0;
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler)
	{		
		//handling Virtual Directories
		// Query Transfermer . Generating symbolic links according to name of virtual directory
		if (path.contains("__"))
		{	
			String queryPortion[] = path.split("__");
			
			//Separating query type and values from  query portion of name
			String q1[] = queryPortion[1].split(":",2);
			String queryType = q1[0].toString();
			String queryValues = q1[1].toString().toLowerCase();
			
			System.out.println("Query Type is: "+queryType);
			System.out.println("Query Value is: "+queryValues);
			
			/*
			//if queryType is getTag then show all files tagged with queryValue
			if (queryType.equals("gettag"))
			{
				System.out.println("getting file tagged with: "+queryValues+" ...");
				
				List<String> taggedFilesURIs= new ArrayList<String>();
				taggedFilesURIs = core360.getURIOfIndividual("#hasTag", queryValues);
				for (int i=0; i<taggedFilesURIs.size() ; i++)
				{
					System.out.println("URI of the file tagged with "+queryValues+" is: "+taggedFilesURIs.get(i));				
					String pathOfFile1 = core360.getPathOfIndividual(taggedFilesURIs.get(i));
					File f1 = new File(pathOfFile1);					
					filler.add(f1.getName()+">>taggedWith_"+queryValues);
					
					pathOfFile1 = mountPoint+pathOfFile1; // make it absolute path
					mapSymLink.put(path+"/"+f1.getName()+">>taggedWith_"+queryValues, pathOfFile1); // to be used in (filling buffer argument of)readLink method for setting target of current symLink
					System.out.println("Map.put("+path+"/"+f1.getName()+">>taggedWith_"+queryValues+" , "+pathOfFile1+")");

				}
			}

			
			//if queryType is getPeakLocation then show all files with peak location as queryValue
			if (queryType.equals("getpeaklocation"))
			{
				System.out.println("getting files having Peak Location: "+queryValues+" ...");
				
				List<String> peakFilesURIs= new ArrayList<String>();
				peakFilesURIs = core360.getURIOfIndividual("#filePeakLocation", queryValues);
				for (int i=0; i<peakFilesURIs.size() ; i++)
				{
					System.out.println("URI of the file having peak location as "+queryValues+" is: "+peakFilesURIs.get(i));				
					String pathOfFile1 = core360.getPathOfIndividual(peakFilesURIs.get(i));
					File f1 = new File(pathOfFile1);					
					filler.add(f1.getName()+">>peakLocation_"+queryValues);
					
					pathOfFile1 = mountPoint+pathOfFile1; // make it absolute path
					mapSymLink.put(path+"/"+f1.getName()+">>peakLocation_"+queryValues, pathOfFile1); // to be used in (filling buffer argument of)readLink method for setting target of current symLink
					System.out.println("Map.put("+path+"/"+f1.getName()+">>peakLocation_"+queryValues+" , "+pathOfFile1+")");
				}
			}
			*/
			
			//get files of which the value of property (queryType) is queryValues (universal, for all properties)
			if (!queryType.isEmpty())
			{
				System.out.println("getting files of which the value of "+queryType+" property is "+queryValues+" ...");
				
				List<String> URIsList= new ArrayList<String>();
				URIsList = core360.getURIOfIndividual("#"+queryType, queryValues, false);
				for (int i=0; i<URIsList.size() ; i++)
				{
					System.out.println("URI of the file having peak location as "+queryValues+" is: "+URIsList.get(i));				
					String pathOfFile1 = core360.getPathOfIndividual(URIsList.get(i));
					File f1 = new File(pathOfFile1);					
					filler.add(f1.getName()+">>"+queryType+"_"+queryValues);
					
					pathOfFile1 = mountPoint+pathOfFile1; // make it absolute path
					mapSymLink.put(path+"/"+f1.getName()+">>"+queryType+"_"+queryValues, pathOfFile1); // to be used in (filling buffer argument of)readLink method for setting target of current symLink
					System.out.println("Map.put("+path+"/"+f1.getName()+">>"+queryType+"_"+queryValues+" , "+pathOfFile1+")");
				}
			}			
		}
		
		//adding fillers to directory
		File f = new File(mirroredFolder+path);
		
		if(f.isDirectory())
		{
			
			 File[] fList = f.listFiles();
			    for (File file : fList) 
			    {
			        filler.add(file.toString());
			    }
			    
			    //creating NOW Folder and TAGs folders on root and in sub folders
			    if (!path.contains("__"))
			    {
			    	if(path.equals("/"))
			    	{
			    		filler.add(nowFolder);
			    		System.out.println("Creating NOW on Root folder");
			    		filler.add(tagsFolder);
			    	}
			    	else
			    	{
					    filler.add(path+nowFolder);
					    filler.add(path+tagsFolder);	
			    	}

			    }
		}
		
		
		//adding fillers to NOW
		if (path.contains(nowFolder) )
		{
			
			//append temporally peak files in NOW
			List<String> list = new ArrayList<String>();		
			Date date = new Date();
			
			list = core360.getURIsOfTemporallyPeakFiles(String.valueOf(date.getDay()), String.valueOf(date.getHours())); //gets list of URIs of those files whose peak day and peak hour are the argument passed to this function
			System.out.println("list ="+list);
			for (int i = 0; i < list.size(); i++)
			{
				System.out.println("URI of individual of which symlink is to be made "+list.get(i));
				
				//extracting file's name from path and create filler (as symLink)of the extracted name
				String pathOfFile = core360.getPathOfIndividual(list.get(i));
				
				//checking if current peak file is in territory of current NOW folder
				Boolean inTerritory= false;
				if (path.equals(nowFolder) )
				{
					System.out.println("path == now folder");
					inTerritory = true;	
				}					
				else 
				{
					System.out.println("else exucuted of path == now folder");
					String p[] = path.split(nowFolder); 
					System.out.println("pathofFile= "+pathOfFile+" p[0]= "+p[0]);
					if (pathOfFile.startsWith(p[0]))
						inTerritory = true;
				}
				 							
				if (inTerritory)
				{
					System.out.println("Temporal based Current Peak File Path= "+pathOfFile);
					File f1 = new File(pathOfFile);
					String tokens[] = core360.splitFileNameIntoBaseAndExtension(f1.getName());
					filler.add(tokens[0]+">>Temporal"+"."+tokens[1]);
					
					pathOfFile = mountPoint+pathOfFile; // make it absolute path
					mapSymLink.put(path+"/"+tokens[0]+">>Temporal"+"."+tokens[1], pathOfFile); // to be used in (filling buffer argument of)readLink method for setting target of current symLink
					System.out.println("Map.put("+path+"/"+f1.getName()+">>Temporal , "+pathOfFile+")");
				}


			}
			
			
			//append GeoLocationPeak files(symLinks) in NOW
			List<String> list1 = new ArrayList<String>();
			
			list1 = core360.getURIOfIndividual("#filePeakLocation", core360.currentLocation, true); //gets list of URIs of those files whose peak location is the current location

			for (int i = 0; i < list1.size(); i++)
			{
				System.out.println("URI of individual of which symlink is to be made "+list1.get(i));
				
				//extracting file's name from path and create filler (as symLink)of the extracted name
				String pathOfFile1 = core360.getPathOfIndividual(list1.get(i));
				
				//checking if current peak file is in territory of current NOW folder
				Boolean inTerritory= false;
				System.out.println("path ="+path +" now folder= "+nowFolder);
				if (path.equals(nowFolder ))
				{
					System.out.println("path == NOW folder (NOW on root = true)");
					inTerritory = true;	
				}
					
				else 
				{
					System.out.println("else exucuted of path == NOW folder (NOW on root = false)");
					String p[] = path.split(nowFolder); 
					System.out.println("pathofFile1= "+pathOfFile1+" p[0]= "+p[0]);
					if (pathOfFile1.startsWith(p[0]))
						inTerritory = true;
				}			
				
				if (inTerritory)
				{
					
					System.out.println("Location based current Peak File Path= "+pathOfFile1);
					File f2 = new File(pathOfFile1);
					String tokens2[] = null;
	
					tokens2 = core360.splitFileNameIntoBaseAndExtension(f2.getName());	
					
					if (tokens2.length>1) // if file name has extension
					{
						filler.add(tokens2[0]+">>Location_"+core360.currentLocation+"."+tokens2[1]);
		
						pathOfFile1 = mountPoint+pathOfFile1; // make it absolute path
						mapSymLink.put(path+"/"+tokens2[0]+">>Location_"+core360.currentLocation+"."+tokens2[1], pathOfFile1); // to be used in (filling buffer argument of)readLink method for setting target of current symLink
						System.out.println("Map.put("+path+"/"+f2.getName()+">>Location_"+core360.currentLocation+" , "+pathOfFile1+")");
					}
					else  //files with no extension
					{
						filler.add(f2.getName()+">>Location_"+core360.currentLocation);
						
						pathOfFile1 = mountPoint+pathOfFile1; // make it absolute path
						mapSymLink.put(path+"/"+f2.getName()+">>Location_"+core360.currentLocation, pathOfFile1); // to be used in (filling buffer argument of)readLink method for setting target of current symLink
						System.out.println("Map.put("+path+"/"+f2.getName()+">>Location_"+core360.currentLocation+" , "+pathOfFile1+")");

					}
				}
			}
			
		}
		
		//adding fillers to TAGs Folder 
		if (path.endsWith(tagsFolder) )
		{
			List<String> tagsList = new ArrayList<String>();
			List<String> URIsList = new ArrayList<String>();

			tagsList = core360.getDataTypeProperty("#hasTag", true); //gets distinct list of all Tags assigned to all files
			System.out.println("List of all Tags= "+tagsList);

			for (int i = 0; i < tagsList.size(); i++)
			{
				//get URIs of all files that are tagged with current i.e. tagsList.get(i)
				URIsList = core360.getURIOfIndividual("#hasTag", tagsList.get(i), true);
				System.out.println("URIs list of files of taged with"+tagsList.get(i)+"= "+URIsList);
				
				//extracting file's name from path and create filler (as symLink)of the extracted name
				for (int j=0; j < URIsList.size(); j++)
				{
					String pathOfFile = core360.getPathOfIndividual(URIsList.get(j));
					
					//checking territory of current TAGs folder
					Boolean inTerritory= false;
					if (path.equals(tagsFolder) )
					{
						System.out.println("path == TAGs folder (TAGs on Root = true)");
						inTerritory = true;	
					}					
					else 
					{
						System.out.println("else exucuted of path == TAGs folder (TAGs on Root= false)");
						String p[] = path.split(tagsFolder); 
						System.out.println("pathofFile= "+pathOfFile+", p[0]= "+p[0]);
						if (pathOfFile.startsWith(p[0]))
							inTerritory = true;
					}
					 							
					if (inTerritory)
					{
						//Adding all tags in TAGs folder located on root
						System.out.println("Name of Tag is: "+tagsList.get(i));
						filler.add("TAG_"+tagsList.get(i));	
					}
				}	

			}											
		} 
		
		
		//adding fillers to sub folders of TAGs Folder 
		if (path.contains("TAG_") )
		{
			List<String> tagsList = new ArrayList<String>();
			List<String> URIsList = new ArrayList<String>();
			
			String tag[] = path.split("TAG_");			
			
			//get distinct URIs of all files that are tagged with current tag i.e. tag[1]
			URIsList = core360.getURIOfIndividual("#hasTag", tag[1], true);
			
			for (int i=0; i < URIsList.size(); i++)
			{				
				//extracting file's name from path and create filler (as symLink)of the extracted name
				String pathOfFile = core360.getPathOfIndividual(URIsList.get(i));
		
				System.out.println("Path of the tagged files= "+pathOfFile);
				File f1 = new File(pathOfFile);
				String tokens[] = core360.splitFileNameIntoBaseAndExtension(f1.getName());
				filler.add(tokens[0]+">>TaggedWith_"+tag[1]+"."+tokens[1]);
				
				pathOfFile = mountPoint+pathOfFile; // make it absolute path
				mapSymLink.put(path+"/"+tokens[0]+">>TaggedWith_"+tag[1]+"."+tokens[1], pathOfFile); // to be used in (filling buffer argument of)readLink method for setting target of current symLink
				System.out.println("Map.put("+path+"/"+f1.getName()+">>TaggedWith_"+tag[1]+pathOfFile+")");
		
			}
			
			
								
		} 
		return 0;
	}

	@Override
	public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info)
	{
		File f = new File(mirroredFolder+path);
		try {
			f.createNewFile();
			mode.setMode(NodeType.FILE, true, true, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("create called: Path="+path);
		
		
		//Check the individual of the newly created file, if not yet created
		File f2 = new File(path);
		System.out.println(path+" File is Hidden="+f2.isHidden());
		if (core360.getURIOfIndividual(path) == null && !f2.isHidden())
		{
			
			//create an individual of File class / of type File
			String uri = core360.createIndividual("#File");
			System.out.println("individual created, resource = "+uri);
						
			//set properties of newly created individual of the File 
			System.out.println("Setting hasPath property ... ");
			core360.setDataTypeProperty(uri,"#hasPath", path);	
			
			System.out.println("Setting Date Created property ...");
			Date date = new Date();
			core360.setDataTypeProperty(uri, "#created", date);
			
			//SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			//core360.setDataTypeProperty(uri, "#created", ft.format(date));

		}
		
		return 0;
	}
	
	@Override
	public int mkdir(final String path, final ModeWrapper mode)
	{
		File f = new File(mirroredFolder+path);
		if (f.exists()) 
		{
			f = null;
			return -ErrorCodes.EEXIST();
		}
		
		else
		{
			f.mkdir();
			
			//Check the individual of the current folder if not yet created
			if (core360.getURIOfIndividual(path) == null)
			{
				
				//create an individual of Folder class / of type Folder
				String uri = core360.createIndividual("#Folder");
				System.out.println("individual created, resouse = "+uri);
				
				
				//set properties of newly created individual of folder 
				System.out.println("Setting hasPath property .. ");
				core360.setDataTypeProperty(uri,"#hasPath", path);	
				
				
			}
		}
		
		return 0;
	}
		
	@Override
	public int open(final String path, final FileInfoWrapper info)
	{		
		System.out.println("open called: Path="+path);

		//Check the individual/URI of the current file, if not yet created (this in case if files are copied directly to mirrorFolder)		
		if (!(path.contains(nowFolder) || path.contains(tagsFolder)))
		{
			File f = new File(path);
			if (core360.getURIOfIndividual(path) == null && !f.isHidden())
			{
				
				//create an individual of File class / of type File
				String uri = core360.createIndividual("#File");
				System.out.println("individual created, resource = "+uri);
								
				//set properties of newly created individual of File 
				System.out.println("Setting hasPath property .. ");
				core360.setDataTypeProperty(uri,"#hasPath", path);			
			}
		}
		
		//setting file accessed Time and location properties if accessed by human
		//ignore setting file accessed property if file is accessed by machine
		Date date = new Date();
		long l = ((date.getTime()-datePreviousFileAccessed.getTime())/1000);
		
		if (l > 3)
		{
			//record entry if different between two file accessed is greater than 3 seconds
			String uri = core360.getURIOfIndividual(path);			
			SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			//core360.setDataTypeProperty(uri, "#fileLastAccessed", ft.format(date));
			core360.setDataTypeProperty(uri, "#fileLastAccessed", date);
			core360.setDataTypeProperty(uri, "#fileAccessedLocation", core360.currentLocation+"_atTime_"+ft.format(date));
			System.out.println("Human request. Accessed time and location recorded");			
			
			
			//Call TemporalDaemon		
			core360.temporalDaemon(uri);
			
			//call Location Daemon
			core360.locationDaemon(uri);
		}
		else
			System.out.println("Automatic request. Accessed time not recorded");

				
		datePreviousFileAccessed = date;
		
		
		return 0;
	}
	

	@Override
	public int rename(final String path, final String newName)
	{
				
		// put restriction on renaming NOW folder
		if (path.contains(nowFolder))
		{
			System.out.println("NOW Folder can not be renamed");
			return -1;
		}
		
		
		File f = new File(mirroredFolder+path);
	    File f1 = new File(mirroredFolder+newName);
	    boolean bool = false;
		
        
        // if the renamed path is a file and contains __ symbols then tag the file and rename the file back to its original name
        if (f.isFile() && newName.contains("__"))
        {
        	System.out.println("Double Under Score detected...");

			String queryPortion[] = newName.split("__");
			core360.handleNameQuery(core360.getURIOfIndividual(path), path, queryPortion[1]);
			
			//return negative, do not rename
			return -1;
        }
		

        //Otherwise if new name do not contain __ symbol then perform rename operation as normal
		else
		{						 		      
		     try{
		    	 
		    	// renaming file or folder	         
		        bool = f.renameTo(f1);		   
		        System.out.print("File/folder renamed? "+bool+" Rename from "+path+" To "+newName);
		        
		        
		        //update hasPath in RDF store
		        File f2 = new File(path);
		        if (!newName.contains("~") && !f2.isHidden())  //when editing and saving a file, Ubuntu delete the current file and create a hidden file and then rename the hidden to deleted(the edited) file. in this i handle to not create URI for these hidden files.
		        	core360.updateDataTypeProperty(core360.getURIOfIndividual(path), "#hasPath", path, newName);
		        
		        
		      }catch(Exception e){
		         // if any error occurs
		         e.printStackTrace();
		      }
		     
		     
		     return 0;
		} 
		
		
	}
	
	@Override
	public int readlink(final String path, final ByteBuffer buffer, final long size)
	{
		Path link = Paths.get(mountPoint+path);
		System.out.println("Link is:" + link);
		System.out.println("Checking isSmymbolic Link condition");
		if (Files.isSymbolicLink(link))
		{

			/*
			try
			{
				System.out.format("Target of link" +
			        " '%s' is '%s'%n", link,
			        Files.readSymbolicLink(link));
				System.out.println("inside try");
			   String s = Files.readSymbolicLink(link).toString();
			    System.out.println("target s ="+s);
				//byte[] b = s.getBytes(Charset.forName("UTF-8"));
				//buffer.put(b);
				
			} catch (IOException x) {
				System.err.println(x);
			} */

			System.out.println("map get. symlink path= "+path+" Target file Path=" +mapSymLink.get(path));
			byte[] b = mapSymLink.get(path).getBytes(Charset.forName("UTF-8"));
			buffer.put(b);
		}
		else
			return -1;
	
		
		
		return 0;
	}
	
	
	@Override
	public int lock(final String path, final FileInfoWrapper info, final FlockCommand command, final FlockWrapper flock)
	{
		return 0;
		//return -ErrorCodes.ENOSYS();
	}
	
	@Override
	public int rmdir(final String path)
	{
		if( path.contains(tagsFolder) && path.contains("TAG_") )
		{
			String[] path1 = path.split("TAG_");
			core360.deleteDataTypeProperty("#hasTag", path1[1]);
		}
		return 0;
	}
}





