package virtualMemorySimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CPU {
	
	public static TLBEntry[] TLBCache = new TLBEntry[16];
	
	
	public static void addTLBEntry (int index, int pageNumber, int vbit, int rbit, int dbit, int frameNumber) {
		
		TLBCache[index] = new TLBEntry(pageNumber, vbit, rbit, dbit, frameNumber);	
	}
	
	public static int nextEmptySpotInTLB () {
		int index = 0;
		for(TLBEntry x: TLBCache) {
			if (x == null)
				return index;
			index++;
		} return -1;
	}
	
	public static int inTLB (int pageNumber) {
		int index = 0;
		for(TLBEntry x: TLBCache) {
			if (x.getVPageNum() == pageNumber)
				return index;
			index++;
		} 
		return -1;	
	}
	
	
	public static void read(String testFilePath) throws FileNotFoundException {
		// reads the test data with loop checking to see if it is a read or write and does 
		
		File readFile = new File(testFilePath);
		Scanner sc = new Scanner(readFile);
		
		int interruptCount = 0;
		
		while(sc.hasNextLine()) {
						
			int temp1 = Integer.valueOf(sc.nextLine());
			
			if(temp1 == 0) {
				//for a read
				
				//read in address here, name variable nextAddress
				String nextAddress = sc.nextLine();
				int intAddress = Integer.parseInt(nextAddress, 16);
				
				//calculate page info and offset for line number
				int pageNumber = (int) Math.floor( intAddress / 256 );
				String pageNumberHex = Integer.toHexString (pageNumber);
				int lineNumber = intAddress - (pageNumber * 256) ;
				
				//if it is found in TLB
				if (inTLB(pageNumber) > -1) {
					int val = PhysicalMemory.read(inTLB(pageNumber), lineNumber);
					VirtualPageTable.getEntry(pageNumber).setRBit(1);
					TLBCache[inTLB(pageNumber)].setRBit(1);
					//write val to CSV
					//record as hit
				}
				//check if entry is in the page table next
				else if (VirtualPageTable.getEntry(pageNumber) != null) {
					//its not in TLB so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 0, index);
						PhysicalMemory.store(index, "Project2_test_and_page_files/page_files_Copy/" + pageNumberHex + ".pg");
						VirtualPageTable.getEntry(pageNumber).setVBit(1); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1); 
						VirtualPageTable.getEntry(pageNumber).setPageFrameNum(index);
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						
					}
					
					int val = PhysicalMemory.read(inTLB(pageNumber), lineNumber);
					//write val to csv
					//record as Soft miss in CSV
				
				}
				
				//else it is a hard miss, come here
				else {

					//its not in TLB so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 0, index);
						PhysicalMemory.store(index, "Project2_test_and_page_files/page_files_Copy/" + pageNumberHex + ".pg");
						VirtualPageTable.store(new PageTableEntry(1, 1, 0, index), pageNumber);
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						
					}
					
					int val = PhysicalMemory.read(inTLB(pageNumber), lineNumber);
					//write val to csv
					//record as hard miss in CSV
				}
				
			}
			
			else if (temp1 == 1) {
				//for a write
				
				//read in address here, name variable nextAddress
				String nextAddress = sc.nextLine();
				int intAddress = Integer.parseInt(nextAddress, 16);
				
				int valToWrite = Integer.valueOf(sc.nextLine());
				
				//calculate page info and offset for line number
				int pageNumber = (int) Math.floor( intAddress / 256 );
				String pageNumberHex = Integer.toHexString (pageNumber);
				int lineNumber = intAddress - (pageNumber * 256) ;
				
				//if it is found in TLB
				if (inTLB(pageNumber) > -1) {
					PhysicalMemory.write(inTLB(pageNumber), lineNumber, valToWrite);
					VirtualPageTable.getEntry(pageNumber).setRBit(1);
					TLBCache[inTLB(pageNumber)].setRBit(1);
					VirtualPageTable.getEntry(pageNumber).setDBit(1);
					TLBCache[inTLB(pageNumber)].setDBit(1);
					//write val to CSV
					//record as hit
				}
				//check if entry is in the page table next
				else if (VirtualPageTable.getEntry(pageNumber) != null) {
					//its not in TLB so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 1, index);
						PhysicalMemory.store(index, "Project2_test_and_page_files/page_files_Copy/" + pageNumberHex + ".pg");
						VirtualPageTable.getEntry(pageNumber).setVBit(1); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1); 
						VirtualPageTable.getEntry(pageNumber).setDBit(1);
						VirtualPageTable.getEntry(pageNumber).setPageFrameNum(index);
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						
					}
					
					PhysicalMemory.write(inTLB(pageNumber), lineNumber, valToWrite);
					//write val to csv
					//record as Soft miss in CSV
				
				}
				
				//else it is a hard miss, come here
				else {

					//its not in TLB so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 1, index);
						PhysicalMemory.store(index, "Project2_test_and_page_files/page_files_Copy/" + pageNumberHex + ".pg");
						VirtualPageTable.store(new PageTableEntry(1, 1, 1, index), pageNumber);
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						
					}
					
					PhysicalMemory.write(inTLB(pageNumber), lineNumber, valToWrite);
					//write val to csv
					//record as hard miss in CSV
				}
				
			
			}
			
			interruptCount++;

			if (interruptCount == 10) {
				//call OS method to reset R bit of all entries in page table
				
			}
			
		}

		sc.close();
		
		// writes to the physical mem
		
		//MMU
			// translates addresses
			// finds page and line #
			
		//TLB
			// 1D array of 16 entries of TLBEntry object
	}

}
