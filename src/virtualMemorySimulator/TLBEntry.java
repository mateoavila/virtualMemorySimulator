package virtualMemorySimulator;

public class TLBEntry {
	private int VPageNum;
	private int vBit;
	private int rBit;
	private int dBit;
	private int pageFrameNum;
	
	public TLBEntry(int VPageNum, int vBit, int rBit, int dBit, int pageFrameNum) {
		this.VPageNum = VPageNum;
		this.vBit = vBit;
		this.rBit = rBit;
		this.dBit = dBit;
		this.pageFrameNum = pageFrameNum;
	}
	
	public int getVPageNum() {
		return VPageNum;
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
