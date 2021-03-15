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
	
	
	public static void fifo(){

	       TLBEntry[] temp = new TLBEntry[16];
	       
	       for(int i = 0; i < (15); i++){
	           temp[i] = TLBCache[i+1];
	       }
	       
	       for(int i = 0; i < 16; i++){
	           TLBCache[i]=temp[i];
	       }
	       
	       TLBCache[15]= null;
	       
	   }
	
	public static void start(String testFilePath) throws FileNotFoundException {
		for (int i = 0; i < 256; i++) {
			VirtualPageTable.pageTable[i] = new PageTableEntry (0,0,0,-1);
		}
		
		// reads the test data with loop checking to see if it is a read or write and does 
		
		File readFile = new File(testFilePath);
		Scanner sc = new Scanner(readFile);
		
		int interruptCount = 0;
		
		while(sc.hasNextLine()) {
						
			int temp1 = Integer.valueOf(sc.nextLine());
			int soft = 0;
			int hard = 0;
			int hit = 0;
			int valRead = 0;
			int valToWrite = 0;
			int[] evictedPageInfo = new int[2];
			
			if(temp1 == 0) {
				//for a read
				
				//read in address here, name variable nextAddress
				String nextAddress = sc.nextLine();
				int intAddress = Integer.parseInt(nextAddress, 16);
				
				//calculate page info and offset for line number
				int pageNumber = (int) Math.floor( intAddress / 256 );
				String pageNumberHex = Integer.toHexString (pageNumber);
				int lineNumber = intAddress - (pageNumber * 256);
				
				//if it is found in TLB and it is marked as active
				if (inTLB(pageNumber) > -1 && TLBCache[inTLB(pageNumber)].getVBit() == 1) {
					valRead = PhysicalMemory.read(TLBCache[inTLB(pageNumber)].getPageFrameNum(), lineNumber);
					VirtualPageTable.getEntry(pageNumber).setRBit(1);
					TLBCache[inTLB(pageNumber)].setRBit(1);
					//record as hit
					hit = 1;
				}
				//check if entry is in the page table next
				else if (VirtualPageTable.getEntry(pageNumber).getVBit() == 1) {
					//its not in TLB so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 0, VirtualPageTable.getEntry(pageNumber).getPageFrameNum());
						VirtualPageTable.getEntry(pageNumber).setRBit(1); 
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						fifo();
						addTLBEntry(15, pageNumber, 1, 1, 0, VirtualPageTable.getEntry(pageNumber).getPageFrameNum()); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1); 
					}
					
					valRead = PhysicalMemory.read(TLBCache[inTLB(pageNumber)].getPageFrameNum(), lineNumber);
					//record as Soft miss in CSV
					soft = 1;
				
				}
				
				//else it is a hard miss, come here
				else {

					//its not in TLB or physical memory so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						evictedPageInfo = OperatingSystem.writeDirtyPage(pageNumberHex);
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 0, VirtualPageTable.getEntry(pageNumber).getPageFrameNum());
						VirtualPageTable.getEntry(pageNumber).setVBit(1); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1); 
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						evictedPageInfo = OperatingSystem.writeDirtyPage(pageNumberHex);
						fifo();
						addTLBEntry(15, pageNumber, 1, 1, 0, VirtualPageTable.getEntry(pageNumber).getPageFrameNum());
						VirtualPageTable.getEntry(pageNumber).setVBit(1); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1);
					}
					
					valRead = PhysicalMemory.read(TLBCache[inTLB(pageNumber)].getPageFrameNum(), lineNumber);
					//record as hard miss in CSV
					hard = 1;
				}
				
			}
			
			else if (temp1 == 1) {
				//for a write
				
				//read in address here, name variable nextAddress
				String nextAddress = sc.nextLine();
				int intAddress = Integer.parseInt(nextAddress, 16);
				
				valToWrite = Integer.valueOf(sc.nextLine());
				
				//calculate page info and offset for line number
				int pageNumber = (int) Math.floor( intAddress / 256 );
				String pageNumberHex = Integer.toHexString (pageNumber);
				int lineNumber = intAddress - (pageNumber * 256) ;
				
				//if it is found in TLB and it is marked as active
				if (inTLB(pageNumber) > -1 && TLBCache[inTLB(pageNumber)].getVBit() == 1) {
					PhysicalMemory.write(TLBCache[inTLB(pageNumber)].getPageFrameNum(), lineNumber, valToWrite);
					VirtualPageTable.getEntry(pageNumber).setRBit(1);
					TLBCache[inTLB(pageNumber)].setRBit(1);
					VirtualPageTable.getEntry(pageNumber).setDBit(1);
					TLBCache[inTLB(pageNumber)].setDBit(1);
					//record as hit
					hit = 1;
				}
				//check if entry is in the page table next
				else if (VirtualPageTable.getEntry(pageNumber).getVBit() == 1) {
					//its not in TLB so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 1, VirtualPageTable.getEntry(pageNumber).getPageFrameNum());						VirtualPageTable.getEntry(pageNumber).setVBit(1); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1); 
						VirtualPageTable.getEntry(pageNumber).setDBit(1);
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						fifo();
						addTLBEntry(15, pageNumber, 1, 1, 1, VirtualPageTable.getEntry(pageNumber).getPageFrameNum());
						VirtualPageTable.getEntry(pageNumber).setRBit(1); 
						VirtualPageTable.getEntry(pageNumber).setDBit(1);
					}
					
					PhysicalMemory.write(inTLB(pageNumber), lineNumber, valToWrite);
					//record as Soft miss in CSV
					soft = 1;
				
				}
				
				//else it is a hard miss, come here
				else {

					//its not in TLB or physical memory so it must be added there
					if ( nextEmptySpotInTLB() > -1 ) {
						evictedPageInfo = OperatingSystem.writeDirtyPage(pageNumberHex);
						int index = nextEmptySpotInTLB();
						//write in new TLB entry here
						addTLBEntry(index, pageNumber, 1, 1, 1, VirtualPageTable.getEntry(pageNumber).getPageFrameNum());
						VirtualPageTable.getEntry(pageNumber).setVBit(1); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1);
						VirtualPageTable.getEntry(pageNumber).setDBit(1);
						
					} else if ( nextEmptySpotInTLB() == - 1 ) {
						//no available spots and one must be overwritten
						evictedPageInfo = OperatingSystem.writeDirtyPage(pageNumberHex);
						fifo();
						addTLBEntry(15, pageNumber, 1, 1, 1, VirtualPageTable.getEntry(pageNumber).getPageFrameNum());
						VirtualPageTable.getEntry(pageNumber).setVBit(1); 
						VirtualPageTable.getEntry(pageNumber).setRBit(1);
						VirtualPageTable.getEntry(pageNumber).setDBit(1);
					}
					
					PhysicalMemory.write(TLBCache[inTLB(pageNumber)].getPageFrameNum(), lineNumber, valToWrite);
					//record as hard miss in CSV
					hard = 1;
				}
				
			
			}
			
			interruptCount++;

			if (interruptCount % 10 == 0) {
				//call OS method to reset R bit of all entries in page table
				OperatingSystem.resetRBit();
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
