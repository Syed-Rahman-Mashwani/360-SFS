# 360-SFS


-----------------------------

360 Semantic File System is developed using FUSE-JNA (FUSE Java Binding). 

Fuse-JNA is downloadable from https://github.com/EtiennePerot/fuse-jna. 


--------------------------

How To Run it (in Eclipse)
-------------------------
Open it  

modify these two variable in SFS_360.java files as per your settings:

e.g.

	static File f_mountPoint = new File ("./target/360FS"); // set mount point (path to a blank folder)
	private final String mirroredFolder = "./target/mirrorredFolder";  //set the path of a folder which you want to be mirrored.	


modify line no. 54 in Core360.java file and give correct path of the ontology file (sfs_Ontology.owl) as per you setings

e.g.

	static final String inputFileName = "./target/resources/sfs_Ontology.owl"; //	path to ontology file


	
Import FUSE-JNA Library

and RUN 

That’s it 



--------------------------

Compatibility
-------------------------

As we used FUSE API, therefore its compatibility is only with Linux based OS.

We have test it on  "Ubuntu 14" 



