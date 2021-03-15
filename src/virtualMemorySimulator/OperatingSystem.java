package virtualMemorySimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class OperatingSystem {

	private static int currentEntry = 0;
	//private static PageTableEntry [] VirtualPageTable.getPageTable() = VirtualPageTable.getPageTable();
	//private static TLBEntry [] CPU.TLBCache = CPU.TLBCache;
	
	// resets the R bit as needed
	public static void resetRBit() {
		
		for(int i = 0; i < VirtualPageTable.getPageTable().length; i++) {
			VirtualPageTable.pageTable[i].setRBit(0);
		}
		for(int j = 0; j < CPU.TLBCache.length; j++) {
			CPU.TLBCache[j].setRBit(0);
		}
	}
	
	
	// writes the dirty page to memory and resets the Dirty bit
	public static int[] writeDirtyPage(String hexPageNum) throws FileNotFoundException { 
		
		
		String freshPage = "Project2_test_and_page_files/page_files_Copy/" + hexPageNum + ".pg";
		int freshPageInt = Integer.parseInt(hexPageNum, 16);
		int evictedPage = -1;
	    int setDirtyVal = -1;
		int [] values = new int [3];
			
		
		//if RAM is full replace page
		if (PhysicalMemory.nextEmptySpotInRAM() == -1) {
			values = pageReplacement();
			
			//write new page to memory
			PhysicalMemory.store(values[0], freshPage);
			
			evictedPage = values[1]; 
			setDirtyVal = values[2]; 
			VirtualPageTable.getPageTable()[freshPageInt].setPageFrameNum(values[0]);
			VirtualPageTable.getPageTable()[freshPageInt].setVBit(1);
        	VirtualPageTable.getPageTable()[freshPageInt].setRBit(1);		

        //if not full add to page table and TLB
		} else {
			int index = PhysicalMemory.nextEmptySpotInRAM();
			PhysicalMemory.store(index, freshPage);
			VirtualPageTable.getPageTable()[freshPageInt].setPageFrameNum(index);
			VirtualPageTable.getPageTable()[freshPageInt].setVBit(1);
        	VirtualPageTable.getPageTable()[freshPageInt].setRBit(1);
			
        	//increment counter in RAM
        	PhysicalMemory.counter++;
		}
		
		// see if this is what needs to be done
		 int[] result = {evictedPage, setDirtyVal};
	     return result;   
	     
	}
	
	
	
	// page table replacement
	public static int[] pageReplacement() throws FileNotFoundException {
		int writeValue = -1;
        int evictedPageLoc = -1;
        int dirtyValue = -1;
       
        StringBuilder sb = new StringBuilder(); // new stringbuilder to store output
        
        boolean clock = true;
        		
        while(clock == true) {
        	//find R bit that is = 0 and reset any other that is not 0
        	//replacement algorithm
        	if(VirtualPageTable.getPageTable()[currentEntry].getRBit() == 1) {
        		VirtualPageTable.getPageTable()[currentEntry].setRBit(0);
        		
        		int t = CPU.inTLB(currentEntry);
        		if( t > -1) {
        			CPU.TLBCache[t].setRBit(0);
        		}
        	}
        	
        	if(VirtualPageTable.getPageTable()[currentEntry].getRBit() == 0 && VirtualPageTable.getPageTable()[currentEntry].getVBit() == 1) {
        		clock = false;
        		break;
        	}
        	if(currentEntry == VirtualPageTable.getPageTable().length - 1) {
        		currentEntry = 0;
        	} else {
        		currentEntry++;
        	}
        }
        
        if(VirtualPageTable.getPageTable()[currentEntry].getDBit() == 1) {
        	String temp = Integer.toHexString(currentEntry);
        	//changes C.pg to 0C.pg
            if(temp.length() == 1){
                temp = "0" + temp;
            }
            PrintWriter pw = new PrintWriter(new File("Project2_test_and_page_files/page_files_Copy/" + temp + ".pg")); // to overwrite the page file
            int spotInRAM = VirtualPageTable.getEntry(currentEntry).getPageFrameNum();
            for(int i=0; i< PhysicalMemory.ram[spotInRAM].length;i++){
                sb.append(PhysicalMemory.ram[spotInRAM][i] + "\n");
            }
            
            pw.write(sb.toString()); pw.close();
            dirtyValue = 1; // dirty was set  
        }
        else{
            dirtyValue = 0; // dirty was not set
        }
        
        evictedPageLoc = currentEntry;
        writeValue = VirtualPageTable.getEntry(currentEntry).getPageFrameNum(); // the open index in RAM
        VirtualPageTable.getPageTable()[currentEntry] = new PageTableEntry(0, 0, 0, -1); 
        
        for (int i = 0; i < CPU.TLBCache.length; i++) {
        	
        	if(CPU.TLBCache[i].getPageFrameNum() == writeValue ) {
   			 	
   	            CPU.TLBCache[i] = new TLBEntry (-1,-1,-1,-1, -1);
   	            break;
   	        } 
        }

		
		// go to next page table entry
        if (currentEntry == VirtualPageTable.getPageTable().length - 1) {
            currentEntry = 0;
        } else {
            currentEntry++;
        }

        int[] resultValues = {writeValue, evictedPageLoc, dirtyValue};
        return resultValues;
	}

}
