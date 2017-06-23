package tomasulo;

// Informações de um registrador
// Funciona como struct
public class Register {
	
	public int id;
	public int reorder;
	public int value;
	public boolean busy;
	
	public Register(int id){
		this.id = id;
		reorder = -1;
		value = 0;
		busy = false;
	}

}
