package virtualMemorySimulator;

public class PageTableEntries {
	private int vBit;
	private int rBit;
	private int dBit;
	private int pageFrameNum;
	
	public PageTableEntries(int vBit, int rBit, int dBit, int pageFrameNum) {
		this.vBit = vBit;
		this.rBit = rBit;
		this.dBit = dBit;
		this.pageFrameNum = pageFrameNum;
	}
	
	public int getVbit() {
		return vBit;
	}
	
	public int getRBit() {
		return rBit;
	}
	
	public int getDBit() {
		return dBit;
	}
	
	public int getPageFrameNum() {
		return pageFrameNum;
	}
}
