package tomasulo;

//Apenas um elemento da Estação de Reserva
//Funciona como struct
public class ReserveStationEntry {
	
	public int id;
	public String type;
	public boolean busy;
	public String instruction;
	public int dest;
	public int vj, vk;
	public int qj, qk;
	public int a;
	public int time;
	public int result;
	
	public ReserveStationEntry(int id, String type){
		this.id = id;
		this.type = type;
		clear();
	}
	
	public void clear(){
		busy = false;
		instruction = "";
		dest = -1;
		vj = vk = -1;
		qj = qk = -1;
		a = -1;
		time = -1;
		result = -1;
	}
	
}
