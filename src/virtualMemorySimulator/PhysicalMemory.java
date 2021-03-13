package virtualMemorySimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class PhysicalMemory {

	private int[][] ram;
	
	public PhysicalMemory() {
		ram = new int[16][256];
	}
	
	public void store(int index, String sourcePath) throws FileNotFoundException {
		File f = new File(sourcePath);
		Scanner sc = new Scanner(f);
		int col = 0;
		while(sc.hasNextInt()) {
			ram[index][col] = sc.nextInt();
			col++;
		}
		sc.close();
	}
	
	public int read(int row, int col) {
		return ram[row][col];
	}
	
	public void write(int row, int col, int val) { 
		ram[row][col] = val;
	}
	
	public int[] getPage(int row) {
		return ram[row];
	}
}
