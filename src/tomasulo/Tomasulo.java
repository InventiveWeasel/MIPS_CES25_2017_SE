package tomasulo;
import java.util.ArrayList;

public class Tomasulo {
	
	private final int REGSIZE = 32;
	private final int ROBSIZE = 1 + 10; // id 0 nao utilizado
	private final int RSSIZE = 1 + 11; // id 0 nao utilizado
	private final int MEMORYSIZE = 4096;
	
	private Register[] RegisterStat;
	private BufferEntry[] ROB;
	private ReserveStationEntry[] RS;
	private DetourBufferEntry[] detourBuffer;
	
	private int[] dataMemory;
	private ArrayList<String> instMemory;
	private int instCount;
	private int pc;
	private int predictionType;
	
	public Tomasulo(ArrayList<String> instructions, int type){
		predictionType = type;
		
		RegisterStat = new Register[REGSIZE];
		for (int i = 0; i < REGSIZE; i++)
			RegisterStat[i] = new Register(i);
		
		ROB = new BufferEntry[ROBSIZE];
		for (int i = 1; i < ROBSIZE; i++)
			ROB[i] = new BufferEntry(i);
		
		RS = new ReserveStationEntry[RSSIZE];
		for (int i = 1; i <= 5; i++) // 5
			RS[i] = new ReserveStationEntry(i, "Load/Store");
		for (int i = 6; i <= 8; i++) // 3
			RS[i] = new ReserveStationEntry(i, "Add");
		for (int i = 9; i < RSSIZE; i++) // 3
			RS[i] = new ReserveStationEntry(i, "Mult");
		
		detourBuffer = new DetourBufferEntry[MEMORYSIZE];
		//for (int i = 0; i < REGSIZE; i++)
		//	RegisterStat[i] = new Register(i);
			
		dataMemory = new int[MEMORYSIZE];
		instMemory = instructions;
		
		instCount = 0;
		pc = 0;
	}
	
	public void run(){
		Timer.reiniciarContador();
		
		while (true){
			int start = Timer.tempoDecorrido();
			
			consolidate();
			execute();
			store();
			execute();
			issue();
			
			int end = Timer.tempoDecorrido();
			
			while(end - start < 1){
				end = Timer.tempoDecorrido();
			}
		}
	}
	
	private void makeDetour(int type){
		int pcAux = pc;
		if(type == 1) //Detour que sempre supõe que segue
			pc += 1;
		else if(type == 2){
			if(detourBuffer[pc].destPC == -1)
				pc += 1;
			else
				pc = detourBuffer[pc].destPC;
		}
		else if(type == 3) //Detour dinâmico de 1 bit
		{
			//Padronizando 0 como seguir
			if(detourBuffer[pc].bitPredictor == 0)
				pc += 1;
			else{
				if(detourBuffer[pc].destPC == -1)
					pc += 1;
				else
					pc = detourBuffer[pc].destPC;
			}
			detourBuffer[pcAux].destPC = pc;
		}
		else if(type == 4){
			if(detourBuffer[pc].bitPredictor == 1 || detourBuffer[pc].bitPredictor == 10)
				pc += 1;
			else{
				if(detourBuffer[pc].destPC == -1)
					pc += 1;
				else
					pc = detourBuffer[pc].destPC;
			}
			detourBuffer[pcAux].destPC = pc; 
		}
	}
	
	private void issue(){
		
		String inst = instMemory.get(pc);
		ArrayList<Object> instInfo = Instructions.getInfo(inst);
		
		Character type = (Character)instInfo.get(0);
		String name = (String)instInfo.get(1);
		String param = (String)instInfo.get(2);
		String category = (String)instInfo.get(3);
		Integer immediate = (Integer)instInfo.get(4);
		Integer address = (Integer)instInfo.get(5);
		Integer rd = (Integer)instInfo.get(6);
		Integer rs = (Integer)instInfo.get(7);
		Integer rt = (Integer)instInfo.get(8);
		
		int b = nextBufferEntry();
		if (b < 0)
			return;
		
		int r = nextReserveStationEntry(category);
		if (r < 0)
			return;
		
		if (type == 'J')
			pc = address;
		
		if (RegisterStat[rs].busy){
			int h = RegisterStat[rs].reorder;
			
			if (ROB[h].state.equals("Write")){
				RS[r].vj = ROB[h].value;
				RS[r].qj = 0;
			}
			else
				RS[r].qj = h;
		}	
		else{
			RS[r].vj = RegisterStat[rs].value;
			RS[r].qj = 0;
		}
		
		if (RegisterStat[rt].busy){
			int h = RegisterStat[rt].reorder;
			
			if (ROB[h].state.equals("Write")){
				RS[r].vk = ROB[h].value;
				RS[r].qk = 0;
			}
			else
				RS[r].qk = h;
		}
		else{
			RS[r].vk = RegisterStat[rt].value;
			RS[r].qk = 0;
		}
		
		RS[r].busy = true;
		RS[r].dest = b;
		RS[r].instruction = name;
		
		ROB[b].instruction = name;
		ROB[b].inst_param = param;
		ROB[b].state = "Issue";
		ROB[b].id = instCount + 1;
		ROB[b].pc = pc;
		
		if (type == 'R'){
			RegisterStat[rd].reorder = b;
			RegisterStat[rd].busy = true;
			ROB[b].dest = rd;
		}
		
		if (name.equals("lw")){
			RS[r].a = immediate;
			RS[r].time = 4;
			RegisterStat[rt].reorder = b;
			RegisterStat[rt].busy = true;
			ROB[b].dest = rt;
		}
		else if (name.equals("sw")){
			RS[r].a = immediate;
			RS[r].time = 4;
		}
		else if (name.equals("mult"))
			RS[r].time = 3;
		else
			RS[r].time = 1;
		
		instCount++;
		
		// Alteração no seguimento do programa, makeDetour faz a análise baseada no que houve no último ciclo do programa		
		if (type == 'I' && (name.equals("beq") || name.equals("ble") || name.equals("bne")))
			makeDetour(predictionType);
		else
			pc++;
	}

	// NAO TERMINADO
	private void execute(){
		for (int r = 1; r < RSSIZE; r++){
			if (!RS[r].busy)
				continue;
			
			if (--RS[r].time > 0)
				continue;
			
			if (RS[r].instruction.equals("lw")){
				// Condicao
				
				// Etapa 1
				
				// Etapa 2
			}
			
			else if (RS[r].instruction.equals("sw")){
				// Condicao
				
				// Codigo
			}
			
			else if (RS[r].qj != 0 || RS[r].qk != 0)
				continue;
			
			if (RS[r].instruction.equals("add"))
				RS[r].result = RS[r].vj + RS[r].vk;
			
			else if (RS[r].instruction.equals("sub"))
				RS[r].result = RS[r].vj - RS[r].vk;
			
			else if (RS[r].instruction.equals("mul"))
				RS[r].result = RS[r].vj * RS[r].vk;
		}
	}

	private void store(){
		int cdb = -1;
		
		for (int r = 1; r < RSSIZE; r++){
			if (!RS[r].busy)
				continue;
			
			int h = RS[r].dest;
			if (!ROB[h].state.equals("Execute") || RS[r].time != 0)
				continue;
			
			if (RS[r].instruction.equals("sw")){
				if (RS[r].qk == 0){
					ROB[h].value = RS[r].vk;
					ROB[h].state = "Write";
				}
			}
			
			else{
				int b = RS[cdb].dest;
				if (cdb < 0 || ROB[h].id < ROB[b].id)
					cdb = r;
			}
			
		}
		
		if (cdb != -1){
			int r = cdb;
			int h = RS[r].dest;
			
			RS[r].busy = false;
			
			for (int x = 1; x < RSSIZE; x++){
				if (RS[x].qj == h){
					RS[x].vj = RS[r].result;
					RS[x].qj = 0;
				}
				if (RS[x].qk == h){
					RS[x].vk = RS[r].result;
					RS[x].qk = 0;
				}
			}
			
			ROB[h].value = RS[r].result;
		}
	}
	
	private void consolidate(){
		int h = 1;
		
		for (int i = 2; i < ROBSIZE; i++){
			if (ROB[i].id < ROB[h].id)
				h = i;
		}
		
		if (!ROB[h].state.equals("Write"))
			return;
		
		int dest = ROB[h].dest;
		
		if (ROB[h].instruction.equals("beq") ||
			ROB[h].instruction.equals("ble") ||
			ROB[h].instruction.equals("bne")){
			// Como verificar se o branch foi mispredicted?
			
			if (detourBuffer[ROB[h].pc].destPC != ROB[h].value){
				pc = ROB[h].value; // fetch PC
				detourBuffer[ROB[h].pc].destPC = ROB[h].value;
				
				if(predictionType == 3){
					if(detourBuffer[ROB[h].pc].bitPredictor == 0)
						detourBuffer[ROB[h].pc].bitPredictor = 1;
					else
						detourBuffer[ROB[h].pc].bitPredictor = 0;
				}
				else if (predictionType == 4){
					switch(detourBuffer[ROB[h].pc].bitPredictor){
					case 11:
						detourBuffer[ROB[h].pc].bitPredictor = 10; break;
					case 10: 
						detourBuffer[ROB[h].pc].bitPredictor = 0; break;
					case 1:
						detourBuffer[ROB[h].pc].bitPredictor = 0; break;
					}	
				}
					
				instCount = ROB[h].id;
				
				for (int b = 1; b < ROBSIZE; b++){
					if (ROB[b].id > ROB[h].id){
						ROB[b].clear();
					
						for (int r = 1; r < RSSIZE; r++){
							if (RS[r].dest == b)
								RS[r].clear();
						}
					}
				}
			}
			else{
				if(predictionType == 4){
					switch(detourBuffer[ROB[h].pc].bitPredictor){
					case 0:
						detourBuffer[ROB[h].pc].bitPredictor = 1; break;
					case 1: 
						detourBuffer[ROB[h].pc].bitPredictor = 11; break;
					case 10:
						detourBuffer[ROB[h].pc].bitPredictor = 11; break;
					}	
				}
			}
			
		}
		
		else if (ROB[h].instruction.equals("sw")){
			int a = ROB[h].a;
			dataMemory[a] = ROB[h].value;
		}
		
		else
			RegisterStat[dest].value = ROB[h].value;
		
		ROB[h].busy = false;
		if (dest >= 0 && RegisterStat[dest].reorder == h)
			RegisterStat[dest].busy = false;
	}
	
	private boolean isFirstLoad(int dest) {
		return false;
	}
	
	private int nextReserveStationEntry(String type){
		for (int i = 1; i < RSSIZE; i++){
			ReserveStationEntry entry = RS[i];
			
			if (entry.type == type && !entry.busy){
				return i;
			}
		}
		
		return -1;
	}
	
	private int nextBufferEntry(){
		int id = instCount % ROBSIZE + 1;
		if (ROB[id].busy)
			return -1;
		return id;
	}
}
