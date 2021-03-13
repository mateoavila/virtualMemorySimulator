package virtualMemorySimulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestClass {

	public static void main(String[] args) throws IOException {
		// make copy of the pages 
		copyDirectory("Project2_test_and_page_files/page_files", "Project2_test_and_page_files/page_files_Copy");
		
		
		// read the 
		// run other classes with the copy 

	}
	
public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation) 
		throws IOException {
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

