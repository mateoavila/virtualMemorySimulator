package virtualMemorySimulator;

public class VirtualPageTable {
	
	public static PageTableEntry[] pageTable = new PageTableEntry[256];
	
	public static void store(PageTableEntry newEntry, int index) {
		// stored in CPU or MMU
		// do calculation to find the page table size
		// 1D array of VPTEntries  with size ^
		pageTable[index] = newEntry;
	}
	
	public static PageTableEntry[] getPageTable() {
		return pageTable;
	}
	
	public static PageTableEntry getEntry(int index) {
		return pageTable[index];
	}
	
	public static void changeEntry(String bit, int index, int newBit) {
		if (bit.equals("vBit")) {
			pageTable[index].setVBit(newBit);
		}
		else if (bit.equals("rBit")) {
			pageTable[index].setRBit(newBit);
		}
		else if (bit.equals("dBit")) {
			pageTable[index].setDBit(newBit);
		}
	}
}
