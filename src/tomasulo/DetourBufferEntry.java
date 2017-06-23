package tomasulo;

// COMENTAR!
public class DetourBufferEntry {
	public int destPC;
	public int bitPredictor;
	
	public DetourBufferEntry(){
		destPC=-1;
		bitPredictor=0;
	}
}
