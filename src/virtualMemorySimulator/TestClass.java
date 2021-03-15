package virtualMemorySimulator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestClass {

	public static void main(String[] args) throws IOException {
		// make copy of the pages 
		//copyDirectory("Project2_test_and_page_files/page_files", "Project2_test_and_page_files/page_files_Copy");
		
		// run other classes with the copy 

	}
	
public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation) throws IOException {
		File copy = new File(sourceDirectoryLocation);
	
		if(copy.isDirectory()) {
			//do nothing 
		}else {
			 Files.walk(Paths.get(sourceDirectoryLocation))
			  .forEach(source -> {
			      Path destination = Paths.get(destinationDirectoryLocation, source.toString()
			        .substring(sourceDirectoryLocation.length()));
			      try {
			          Files.copy(source, destination);
			      } catch (IOException e) {
			          e.printStackTrace();
			      }
			  });
		}
	}
}

