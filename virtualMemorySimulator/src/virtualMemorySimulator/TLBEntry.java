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
	
	public int getVBit() {
		return vBit;
	}
	
	public void setVBit(int newVBit) {
		vBit = newVBit;
	}
	
	public int getRBit() {
		return rBit;
	}
	
	public void setRBit(int newRBit) {
		vBit = newRBit;
	}
	
	public int getDBit() {
		return dBit;
	}
	
	public void setDBit(int newDBit) {
		vBit = newDBit;
	}
	
	public int getPageFrameNum() {
		return pageFrameNum;
	}

}
