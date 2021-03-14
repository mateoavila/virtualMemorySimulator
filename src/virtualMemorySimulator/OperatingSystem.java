package virtualMemorySimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class OperatingSystem {

	private static int pointer = 0;
	private static PageTableEntry [] VPT = VirtualPageTable.getPageTable();
	private static TLBEntry [] tlb = CPU.TLBCache;
	
	// resets the R bit as needed
	public static void resetRBit() {
		
		for(int i = 0; i < VPT.length; i++) {
			VirtualPageTable.pageTable[i].setRBit(0);
		}
		for(int j = 0; j < CPU.TLBCache.length; j++) {
			tlb[j].setRBit(0);
		}
	}
	
	
	// writes the dirty page to memory and resets the Dirty bit
	public static int[] writeDirtyPage(String hexPageNum) throws FileNotFoundException { 
		
		int [] pgFile = new int[256];
		String dirtyPage = "Project2_test_and_page_files/page_files_Copy/" + hexPageNum + ".pg";
		int dirtyPageInt = Integer.parseInt(hexPageNum, 16);
		Scanner sc = new Scanner(new File(dirtyPage));
		int evictedPage = -1;
	    int dirtySet = -1;
		int [] replace = new int [2];
		
		//reads from page file and stores the content in an array
		for (int i = 0; i < pgFile.length; i++) {
			if(sc.hasNext()) {
				pgFile[i] = sc.nextInt();
			}
		}
		
		sc.close();
		
		
		//if RAM is full replace page
		if (PhysicalMemory.ram.length == 16) {
			replace = pageReplacement(replace); 
			evictedPage = replace[1]; 
			dirtySet = replace[2]; 
			VPT[dirtyPageInt].setPageFrameNum(replace[0]);
			VPT[dirtyPageInt].setDBit(dirtySet);
			
            
            if(CPU.inTLB(dirtyPageInt) == 1) {
            	//change this if not correct
            	CPU.addTLBEntry(evictedPage, evictedPage, 1, 1, dirtySet, dirtyPageInt); 
            }
        //if not full add to page table and TLB
		}else {
			//change this if not correct
			CPU.addTLBEntry(evictedPage, evictedPage, 1, 1, dirtySet, dirtyPageInt);// should be empty index
			
			 if(CPU.inTLB(dirtyPageInt) == 1) {
				//change this if not correct
				 CPU.addTLBEntry(evictedPage, evictedPage, 1, 1, dirtySet, dirtyPageInt);
	         }
		}
		
		// see if this is what needs to be done
		 int[] result = {evictedPage, dirtySet};
	     return result;
	}
	
	
	
	// does clock replacment for the page table
	public static int[] pageReplacement(int [] replace) throws FileNotFoundException {
		int writeIndex = -1;
        int evictedPage = -1;
        int dirtySet = -1;
       
        StringBuilder sb = new StringBuilder(); // new stringbuilder to store output
        
        boolean cycle = true;
        		
        while(cycle == true) {
        	//find R bit that is = 0 and reset any other that is not 0
        	//replacement algorithm
        	if(VPT[pointer].getRBit() == 1) {
        		VPT[pointer].setRBit(0);
        		
        		if(tlb[pointer].getRBit() == 1) {
        			tlb[pointer].setRBit(0);
        		}
        	}
        	
        	if(VPT[pointer].getRBit() == 1 && VPT[pointer].getVBit() == 1) {
        		cycle = false;
        		break;
        	}
        	if(pointer == VPT.length -1) {
        		pointer = 0;
        	}else {
        		pointer++;
        	}
        }
        
        if(VPT[pointer].getDBit() == 1) {
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
        VPT[pointer] = new PageTableEntry(0, 0, 0, pointer); // fix this
        
		if(tlb[pointer] != null) {
			 String tmp = Integer.toHexString(pointer);
	            if (tmp.length() == 1) {
	                tmp = "0" + tmp;
	            }
	            int tlbevict = Integer.parseInt(tmp, 16);
	            tlb[tlbevict] = null;
	        }
		
		// go to next page table entry
        if (pointer == VPT.length - 1) {
            pointer = 0;
        } else {
            pointer++;
        }

        int[] ret = {writeIndex, evictedPage, dirtySet};
        return ret;
	}

}
