package virtualMemorySimulator;

//import statements
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CPU {
	
	//initialize TLB of size 16
	public static TLBEntry[] TLBCache = new TLBEntry[16];
	
	//method to add a new TLBEntry with all required values as parameters
	public static void addTLBEntry (int index, int pageNumber, int vbit, int rbit, int dbit, int frameNumber) {
		//add a new entry at the specified index with desired values
		TLBCache[index] = new TLBEntry(pageNumber, vbit, rbit, dbit, frameNumber);	
	}
	
	//method to find and return the next empty spot in the TLB,
	//if no empty spots exist then return -1 instead.
	public static int nextEmptySpotInTLB () {
		//define counter variable
		int index = 0;
		//iterate through the TLB for every TLBEntry 
		for(TLBEntry x: TLBCache) {
			//if there is open spot found, return its index
			if (x.getVBit() == -1)
				return index;
			//if not empty, increment counter and continue
			index++;
		} 
		//if no empty spots found, return -1
		return -1;
	}
	
	//method to find out whether an entry with the specified 
	//page number is currently in the TLB or not, returns -1 if not
	//or the index if it is found
	public static int inTLB (int pageNumber) {
		//define counter variable
		int index = 0;
		//iterate through TLB for every TLBEntry
		for(TLBEntry x: TLBCache) {
			//if found, return index
			if( x.getVBit() != -1 ) {
				if (x.getVPageNum() == pageNumber)
					return index;
			} 
			//increment counter variable
			index++;
		} 
		//if not found, return -1 instead
		return -1;	
	}
	
	//FIFO method to copy values into one index lower, then clear last index for use
	public static void fifo(){

		   //define temp array to copy values over into
	       TLBEntry[] temp = new TLBEntry[16];
	       
	       //iterate through TLB and copy values into temp array
	       for(int i = 0; i < (15); i++){
	           temp[i] = TLBCache[i+1];
	       }
	       
	       //copy values from temp array back into TLB at one index lower than before
	       for(int i = 0; i < 16; i++){
	           TLBCache[i]=temp[i];
	       }
	       
	       //reset the last entry in TLB to make ready for new entry
	       TLBCache[15]= new TLBEntry (-1,-1,-1,-1, -1);;
	       
	   }
	
	public static void start(String testFilePath, String outputFilePath) throws IOException {
		// At the very beginning, iterate through all page table entries
		// and initialize all page frame numbers to -1.
		for (int i = 0; i < 256; i++) {
			VirtualPageTable.pageTable[i] = new PageTableEntry (0,0,0,-1);
		}
		
		TLBCache = new TLBEntry[16];
		
		//initialize all to -1 at the start
		for (int i = 0; i < TLBCache.length; i++) {
			
			TLBCache[i] = new TLBEntry (-1,-1,-1,-1, -1);
		}
		
		//create filewriter for writing to CSV
		FileWriter csvWriter = new FileWriter(outputFilePath);
		
		//append statements for column headers
		csvWriter.append("Address");
        csvWriter.append(",");
        csvWriter.append("r/w");
        csvWriter.append(",");
        csvWriter.append("value");
        csvWriter.append(",");
        csvWriter.append("soft");
        csvWriter.append(",");
        csvWriter.append("hard");
        csvWriter.append(",");
        csvWriter.append("hit");
        csvWriter.append(",");
        csvWriter.append("evicted_pg#");
        csvWriter.append(",");
        csvWriter.append("dirty_evicted_page");
        csvWriter.append("\n");
		
		// reads the test data with loop checking to see if it is a read or write, reacts accordingly
		// define file and scanner here
		File readFile = new File(testFilePath);
		Scanner sc = new Scanner(readFile);
		
		int interruptCount = 0;
		
		while(sc.hasNextLine()) {
						
			int temp1 = Integer.valueOf(sc.nextLine());
			String nextAddress = "";
			int soft = 0;
			int hard = 0;
			int hit = 0;
			int valRead = 0;
			int valToWrite = 0;
			int[] evictedPageInfo = new int[2];
			
			if(temp1 == 0) {
				//for a read
				
				//read in address here, name variable nextAddress
				nextAddress = sc.nextLine();
				int intAddress = Integer.parseInt(nextAddress, 16);
				
				//calculate page info and offset for line number
				int pageNumber = (int) Math.floor( intAddress / 256 );
				String pageNumberHex = Integer.toHexString (pageNumber);
				if (pageNumberHex.length() == 1) {
					pageNumberHex = "0" + pageNumberHex;
				}
				int lineNumber = intAddress - (pageNumber * 256);
				
				//if it is found in TLB and it is marked as active (hit)
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
				nextAddress = sc.nextLine();
				int intAddress = Integer.parseInt(nextAddress, 16);
				
				valToWrite = Integer.valueOf(sc.nextLine());
				
				//calculate page info and offset for line number
				int pageNumber = (int) Math.floor( intAddress / 256 );
				String pageNumberHex = Integer.toHexString (pageNumber);
				if (pageNumberHex.length() == 1) {
					pageNumberHex = "0" + pageNumberHex;
				}
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
						addTLBEntry(index, pageNumber, 1, 1, 1, VirtualPageTable.getEntry(pageNumber).getPageFrameNum()); 
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
			
			csvWriter.append(nextAddress);
	        csvWriter.append(",");
	        String temptemp = String.valueOf(temp1);
	        csvWriter.append(temptemp);
	        csvWriter.append(",");
	        if ( temp1 == 0 ) {
	        	String a = String.valueOf(valRead);
	        	csvWriter.append(a);
	        } else if ( temp1 == 1 ) {
	        	String b = String.valueOf(valToWrite);
	        	csvWriter.append(b);
	        }
	        
	        //make CSV appends, using temptemp as necessary to convert variable types
	        csvWriter.append(",");
	        temptemp = String.valueOf(soft);
	        csvWriter.append(temptemp);
	        csvWriter.append(",");
	        temptemp = String.valueOf(hard);
	        csvWriter.append(temptemp);
	        csvWriter.append(",");
	        temptemp = String.valueOf(hit);
	        csvWriter.append(temptemp);
	        csvWriter.append(",");
	        if( evictedPageInfo[0] > -1) {
	        	temptemp = Integer.toHexString (evictedPageInfo[0]);
	        	csvWriter.append(temptemp);
	        } else {
	        	temptemp = "N/A";
	        	csvWriter.append(temptemp);
	        }
	        csvWriter.append(",");
	        temptemp = String.valueOf(evictedPageInfo[1]);
	        csvWriter.append(temptemp);
	        csvWriter.append("\n");
			
		}

		sc.close();
		csvWriter.close();
	}

}
