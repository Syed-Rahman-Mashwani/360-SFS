# 360-SFS


-----------------------------

Java Fuse Mirror File System is developed using FUSE-JNA (FUSE Java Binding). 

Fuse-JNA is downloadable from https://github.com/EtiennePerot/fuse-jna. 


--------------------------

How To Run it (in Eclipse)
-------------------------
Open it  

modify first these two variable in SFS_360.java files as per your settings:

e.g.

	static File f_mountPoint = new File ("./target/360FS"); // set mount point (path to a blank folder)
	private final String mirroredFolder = "./target/mirrorredFolder";  //set the path of a folder which you want to be mirrored.	

	
Import FUSE-JNA Library

and RUN 

That’s it 


