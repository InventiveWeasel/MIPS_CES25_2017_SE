package tomasulo;
public class BufferEntry {
	
	public int id;
	public boolean busy;
	public String instruction;
	public String inst_param;
	public String state;
	public int dest;
	public int value;
	
	public int a;
	public int pc;
	
	public BufferEntry(int id){
		this.id = id;
		clear();
	}
	
	public void clear(){
		busy = false;
		instruction = "";
		inst_param = "";
		state = "";
		dest = -1;
		value = -1;
		a = -1;
		pc = -1;
	}
	
}
