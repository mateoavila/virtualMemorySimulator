package virtualMemorySimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class OperatingSystem {

	private static int pointer = 0;
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
		int dirtyPageInt = Integer.parseInt(hexPageNum, 16);
		int evictedPage = -1;
	    int dirtySet = -1;
		int [] replace = new int [3];
			
		
		//if RAM is full replace page
		if (PhysicalMemory.nextEmptySpotInRAM() == -1) {
			replace = pageReplacement();
			
			//write new page to memory
			PhysicalMemory.store(replace[0], freshPage);
			
			evictedPage = replace[1]; 
			dirtySet = replace[2]; 
			VirtualPageTable.getPageTable()[dirtyPageInt].setPageFrameNum(replace[0]);
			
            if (CPU.inTLB(dirtyPageInt) != -1) {
            	
            	CPU.TLBCache[CPU.inTLB(dirtyPageInt)].setPageFrameNum(replace[0]);
            	
            }
			

        //if not full add to page table and TLB
		} else {
			int index = PhysicalMemory.nextEmptySpotInRAM();
			PhysicalMemory.store(index, freshPage);
			VirtualPageTable.getPageTable()[dirtyPageInt].setPageFrameNum(index);
			
			if (CPU.inTLB(dirtyPageInt) != -1) {
            	
            	CPU.TLBCache[CPU.inTLB(dirtyPageInt)].setPageFrameNum(replace[0]);
            	
            }
		}
		
		// see if this is what needs to be done
		 int[] result = {evictedPage, dirtySet};
	     return result;   
	     
	}
	
	
	
	// page table replacement
	public static int[] pageReplacement() throws FileNotFoundException {
		int writeIndex = -1;
        int evictedPage = -1;
        int dirtySet = -1;
       
        StringBuilder sb = new StringBuilder(); // new stringbuilder to store output
        
        boolean cycle = true;
        		
        while(cycle == true) {
        	//find R bit that is = 0 and reset any other that is not 0
        	//replacement algorithm
        	if(VirtualPageTable.getPageTable()[pointer].getRBit() == 1) {
        		VirtualPageTable.getPageTable()[pointer].setRBit(0);
        		
        		if(CPU.TLBCache[pointer].getRBit() == 1) {
        			CPU.TLBCache[pointer].setRBit(0);
        		}
        	}
        	
        	if(VirtualPageTable.getPageTable()[pointer].getRBit() == 0 && VirtualPageTable.getPageTable()[pointer].getVBit() == 1) {
        		cycle = false;
        		break;
        	}
        	if(pointer == VirtualPageTable.getPageTable().length - 1) {
        		pointer = 0;
        	} else {
        		pointer++;
        	}
        }
        
        if(VirtualPageTable.getPageTable()[pointer].getDBit() == 1) {
        	String tmp = Integer.toHexString(pointer);
        	//changes C.pg to 0C.pg
            if(tmp.length() == 1){
                tmp = "0" + tmp;
            }
            PrintWriter pw = new PrintWriter(new File("Project2_test_and_page_files/page_files_Copy/" + tmp + ".pg")); // to overwrite the page file
            int RAMindex = VirtualPageTable.getEntry(pointer).getPageFrameNum();
            for(int i=0; i< PhysicalMemory.ram[RAMindex].length;i++){
                sb.append(PhysicalMemory.ram[RAMindex][i] + "\n");
            }
            
            pw.write(sb.toString()); pw.close();
            dirtySet = 1; // dirty was set  
        }
        else{
            dirtySet = 0; // dirty was not set
        }
        
        evictedPage = pointer;
        writeIndex = VirtualPageTable.getEntry(pointer).getPageFrameNum(); // the open index in RAM
        VirtualPageTable.getPageTable()[pointer] = new PageTableEntry(0, 0, 0, -1); // fix this
        
        for (int i = 0; i < CPU.TLBCache.length; i++) {
        	
        	if(CPU.TLBCache[i].getPageFrameNum() == writeIndex ) {
   			 	
   	            CPU.TLBCache[i] = null;
   	            break;
   	        } 
        }

		
		// go to next page table entry
        if (pointer == VirtualPageTable.getPageTable().length - 1) {
            pointer = 0;
        } else {
            pointer++;
        }

        int[] ret = {writeIndex, evictedPage, dirtySet};
        return ret;
	}

}
