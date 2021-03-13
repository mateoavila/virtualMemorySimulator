package virtualMemorySimulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteCSV {

	public static void saveTitle(String text) throws IOException {
		
		File file1 = new File("Result.csv");
		
		FileWriter fw = new FileWriter(file1, true); // true will append the data in the csv 
		
		PrintWriter pw = new PrintWriter(fw);
		
		pw.print(text); // this is just a print not a println so we need to make a new line after dirty_evicted_page
		
		pw.close();
	}
}
