package tomasulo;
import GUI.appGUI;
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
	public String[][] RSMatrix;
	public String[][] ROBMatrix;
	public String[][] RegisterStatMatrix;
	public String[][] ExecutionDataMatrix;
	public String[][] RecentUsedMemoryMatrix;
	
	private int[] dataMemory;
	private ArrayList<String> instMemory;
	private int instCount;
	private int pc;
	private int predictionType;
	private appGUI gui;
	
	public Tomasulo(ArrayList<String> instructions, int type, appGUI frame){
		gui = frame; 
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
		
		detourBuffer = new DetourBufferEntry[instructions.size()];
		for (int i = 0; i < instructions.size(); i++)
			detourBuffer[i] = new DetourBufferEntry();
		
		ROBMatrix = new String[ROBSIZE-1][6];
		RegisterStatMatrix = new String[8][12];
		RSMatrix = new String[RSSIZE-1][10];
		ExecutionDataMatrix = new String[4][2];
		RecentUsedMemoryMatrix = new String[4][2];
		
		ExecutionDataMatrix[0][0] = "Clock Corrente";
		ExecutionDataMatrix[1][0] = "PC";
		ExecutionDataMatrix[2][0] =	"Número de Instruções Contadas";
		ExecutionDataMatrix[3][0] = "Clock por Instrução (CPI)";
		
		dataMemory = new int[MEMORYSIZE];
		instMemory = instructions;
		
		instCount = 0;
		pc = 0;
	}
	
	public void updateTables(){
		//Update RSMatrix
		for(int i=1; i < RSSIZE; i++){
			RSMatrix[i-1][0] = "ER" + i;
			RSMatrix[i-1][1] = RS[i].type;
			RSMatrix[i-1][2] = RS[i].busy ? "Sim" : "Não";
			RSMatrix[i-1][3] = RS[i].instruction;
			RSMatrix[i-1][4] = "#" + RS[i].dest;
			RSMatrix[i-1][5] = "" + RS[i].vj;
			RSMatrix[i-1][6] = "" + RS[i].vk;
			RSMatrix[i-1][7] = "" + RS[i].qj;
			RSMatrix[i-1][8] = "" + RS[i].qk;
			RSMatrix[i-1][9] = "" + RS[i].a;
		}
		
		gui.updateResStatTable(RSMatrix);
		
		for(int i=1; i< ROBSIZE; i++){
			ROBMatrix[i-1][0] = "" + ROB[i].id;
			ROBMatrix[i-1][1] = ROB[i].busy ? "Sim" : "Não";
			ROBMatrix[i-1][2] = ROB[i].instruction + " " + ROB[i].inst_param;
			ROBMatrix[i-1][3] = ROB[i].state;
			ROBMatrix[i-1][4] = "R" + ROB[i].dest;
			ROBMatrix[i-1][5] = "" + ROB[i].value;
		}
		
		gui.updateReordBufTable(ROBMatrix);
		
		for(int i=0; i < 8; i++){
			RegisterStatMatrix[i][0] = "R" + i;
			RegisterStatMatrix[i][3] = "R" + i + 8;
			RegisterStatMatrix[i][6] = "R" + i + 16;
			RegisterStatMatrix[i][9] = "R" + i + 2;
			RegisterStatMatrix[i][1] = "#" + RegisterStat[i].reorder;
			RegisterStatMatrix[i][2] = "" + RegisterStat[i].value;	
			RegisterStatMatrix[i][4] = "#" + RegisterStat[i+8].reorder;
			RegisterStatMatrix[i][7] = "#" + RegisterStat[i+16].reorder;
			RegisterStatMatrix[i][10] = "#" + RegisterStat[i+24].reorder;
			RegisterStatMatrix[i][5] = "" + RegisterStat[i+8].value;
			RegisterStatMatrix[i][8] = "" + RegisterStat[i+16].value;
			RegisterStatMatrix[i][11] = "" + RegisterStat[i+24].value;
		}
		
		gui.updateRegsTable(RegisterStatMatrix);
		
		ExecutionDataMatrix[0][1] = "" + Timer.tempoDecorrido();
		ExecutionDataMatrix[1][1] =	"" + pc;
		ExecutionDataMatrix[2][1] =	"" + instCount;
		if(Timer.tempoDecorrido() == 0)
			ExecutionDataMatrix[3][1] = ""; 
		else
			ExecutionDataMatrix[3][1] =	"" + instCount/Timer.tempoDecorrido();
		
		gui.updateExecTable(ExecutionDataMatrix);
		
		/*RecentUsedMemoryMatrix[3][0] = RecentUsedMemoryMatrix[2][0];
		RecentUsedMemoryMatrix[2][0] = RecentUsedMemoryMatrix[1][0];
		RecentUsedMemoryMatrix[1][0] = RecentUsedMemoryMatrix[0][0];
		RecentUsedMemoryMatrix[0][0] = ;*/
		
	}
	
	public void run(){
		Timer.reiniciarContador();
		updateTables();
		
		while (true){
			int start = Timer.tempoDecorrido();
			int end = Timer.tempoDecorrido();
			
			while(end - start < 1){
				end = Timer.tempoDecorrido();
			}
			
			consolidate();
			store();
			execute();
			if (pc < instMemory.size())
				issue();
			updateTables();

		}
	}
	
	private void makeDetour(){
		int pcAux = pc;
		if(predictionType == 1) //Detour que sempre supõe que segue
			pc += 1;
		else if(predictionType == 3) //Detour dinâmico de 1 bit
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
		else if(predictionType == 4){
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
		
		if (type == 'J')
			pc = address;
		
		int b = nextBufferEntry();
		if (b < 0)
			return;
		
		int r = nextReserveStationEntry(category);
		if (r < 0)
			return;
		
		RS[r].clear();
		ROB[b].clear();
		
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
		ROB[b].busy = true;
		
		if (type == 'R'){
			RegisterStat[rd].reorder = b;
			RegisterStat[rd].busy = true;
			ROB[b].dest = rd;
		}
		else if (type == 'I'){
			RS[r].a = immediate;
			ROB[b].dest = rt;
		}
		
		if (name.equals("lw")){
			RS[r].time = 4;
			RegisterStat[rt].reorder = b;
			RegisterStat[rt].busy = true;
			ROB[b].dest = rt;
		}
		else if (name.equals("sw")){
			RS[r].time = 4;
		}
		else if (name.equals("mul"))
			RS[r].time = 3;
		else
			RS[r].time = 1;
		
		instCount++;
		
		// Alteração no seguimento do programa, makeDetour faz a análise baseada no que houve no último ciclo do programa		
		if (name.equals("beq") || name.equals("ble") || name.equals("bne"))
			makeDetour();
		else
			pc++;
	}

	private void execute(){
		int loadStep2 = -1;
		
		for (int r = 1; r < RSSIZE; r++){
			if (!RS[r].busy)
				continue;
			
			int h = RS[r].dest;
			
			if (ROB[h].state.equals("Issue")){
				boolean startCondition = false;
				
				if (RS[r].instruction.equals("sw"))
					startCondition = (h == bufferHead() && RS[r].qj == 0);
				
				else if (RS[r].equals("lw"))
					startCondition = (noStoresBefore(h) && RS[r].qj == 0);
				
				// FP op. (discutivel?)
				else
					startCondition = (RS[r].qj == 0 && RS[r].qk == 0);
				
				if (startCondition)
					ROB[h].state = "Execute";
			}
			
			if (RS[r].time <= 0)
				continue;
			
			if (ROB[h].state.equals("Execute")){
				
				if (RS[r].instruction.equals("lw")){
					// Ainda na primeira etapa
					if (RS[r].time > 1){
						if (--RS[r].time == 1)
							RS[r].a = RS[r].vj + RS[r].a;
					}
					
					// Aguardando segunda etapa iniciar
					else if (RS[r].time == 1){
						if (noStoresBeforeWithAddress(h, RS[r].a)){
							if (loadStep2 != -1){
								int b = RS[loadStep2].dest;
								if (h < b)
									loadStep2 = r;
							}
							else loadStep2 = r;
						}
					}
					
					// Durante segunda etapa
					else RS[r].time--;
				}
				
				else{
					if (--RS[r].time != 0)
						continue;
					
					if (RS[r].instruction.equals("add"))
						RS[r].result = RS[r].vj + RS[r].vk;
					
					else if (RS[r].instruction.equals("sub"))
						RS[r].result = RS[r].vj - RS[r].vk;
					
					else if (RS[r].instruction.equals("mul"))
						RS[r].result = RS[r].vj * RS[r].vk;
					
					else if (RS[r].instruction.equals("addi"))
						RS[r].result = RS[r].vj + RS[r].a;
					
					else if (RS[r].instruction.equals("sw"))
						ROB[h].a = RS[r].vj + RS[r].a;
					
					else if (RS[r].instruction.equals("beq"))
						if (RS[r].vj == RS[r].vk)
							RS[r].result = ROB[h].pc + RS[r].a / 4 + 1;
						else
							RS[r].result = ROB[h].pc + 1;
					
					else if (RS[r].instruction.equals("ble"))
						if (RS[r].vj <= RS[r].vk)
							RS[r].result = RS[r].a / 4;
						else
							RS[r].result = ROB[h].pc + 1;
					
					else if (RS[r].instruction.equals("bne"))
						if (RS[r].vj != RS[r].vk)
							RS[r].result = ROB[h].pc + RS[r].a / 4 + 1;
						else
							RS[r].result = ROB[h].pc + 1;
				}
				
			}
		}
		
		// Segunda etapa do load
		if (loadStep2 != -1){
			int r = loadStep2;
			int a = RS[r].a;
			RS[r].time--;
			RS[r].result = dataMemory[a];
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
					RS[r].busy = false;
				}
			}
			
			else{
				if (cdb != -1){
					int b = RS[cdb].dest;
					if (ROB[h].id < ROB[b].id)
						cdb = r;
				}
				else cdb = r;
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
			ROB[h].state = "Write";
		}
	}
	
	private void consolidate(){
		int h = bufferHead();
		
		if (h < 0)
			return;
		
		if (!ROB[h].state.equals("Write"))
			return;
		
		int dest = ROB[h].dest;
		
		if (ROB[h].instruction.equals("beq") ||
			ROB[h].instruction.equals("ble") ||
			ROB[h].instruction.equals("bne")){
			// Como verificar se o branch foi mispredicted?
			
			if (detourBuffer[ROB[h].pc].destPC != ROB[h].value){
				System.out.println("ERRRRRRROUUUU\n");
				pc = ROB[h].value; // fetch PC
				if(predictionType == 3 || predictionType == 4)
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
						
						for (int r = 0; r < REGSIZE; r++){
							if (RegisterStat[r].reorder == b){
								RegisterStat[r].busy = false;
								RegisterStat[r].reorder = -1;
							}
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
		ROB[h].state = "Commit";
		if (dest >= 0 && RegisterStat[dest].reorder == h)
			RegisterStat[dest].busy = false;
	}
	
	private boolean noStoresBefore(int l){
		for (int b = 1; b < ROBSIZE; b++)
			if (ROB[b].id < ROB[l].id && ROB[b].instruction.equals("sw"))
				return false;
		
		return true;
	}
	
	private boolean noStoresBeforeWithAddress(int l, int a){
		for (int b = 1; b < ROBSIZE; b++)
			if (ROB[b].a == a &&
				ROB[b].id < ROB[l].id &&
				ROB[b].instruction.equals("sw") &&
				ROB[b].busy)
				return false;
		
		return true;
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
		int id = instCount % (ROBSIZE - 1) + 1;
		if (ROB[id].busy)
			return -1;
		return id;
	}

	private int bufferHead(){
		int head = -1;
		
		for (int i = 1; i < ROBSIZE; i++){
			if (!ROB[i].busy)
				continue;
			
			if (head != -1){
				if (ROB[i].id < ROB[head].id)
					head = i;
			}
			else head = i;
		}
		
		return head;
	}
}
