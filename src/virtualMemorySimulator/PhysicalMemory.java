package virtualMemorySimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class PhysicalMemory {

	public static int[][] ram= new int[16][256];
	public static int counter = 0;

	public static void store(int index, String sourcePath) throws FileNotFoundException {
		File f = new File(sourcePath);
		Scanner sc = new Scanner(f);
		int col = 0;
		while(sc.hasNextInt()) {
			ram[index][col] = sc.nextInt();
			col++;
		}
		sc.close();
	}
	
	public static int nextEmptySpotInRAM () {
			if ( counter >= 16 )
				return -1;
			
			return counter;
	}
	
	public static int read(int row, int col) {
		return ram[row][col];
	}
	
	public static void write(int row, int col, int val) { 
		ram[row][col] = val;
	}
	
	public static int[] getPage(int row) {
		return ram[row];
	}
}
