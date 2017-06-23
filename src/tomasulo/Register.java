package tomasulo;
public class Register {
	
	private int id;
	public int reorder;
	public int value;
	public boolean busy;
	
	public Register(int id){
		this.id = id;
		reorder = -1;
		value = -1;
		busy = false;
	}

}
