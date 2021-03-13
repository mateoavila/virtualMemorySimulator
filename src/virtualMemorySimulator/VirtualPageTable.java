package virtualMemorySimulator;

public class VirtualPageTable {
	
	private PageTableEntry[] pageTable;
	
	public VirtualPageTable() {
		this.pageTable = new PageTableEntry[256];
	}

	public void store(PageTableEntry newEntry, int index) {
		// stored in CPU or MMU
		// do calculation to find the page table size
		// 1D array of VPTEntries  with size ^
		pageTable[index] = newEntry;
	}
	
	public PageTableEntry[] getPageTable() {
		return pageTable;
	}
	
	public PageTableEntry getEntry(int index) {
		return pageTable[index];
	}
	
	public void changeEntry(String bit, int index, int newBit) {
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
