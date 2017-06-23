package tomasulo;
import GUI.appGUI;
import java.util.ArrayList;

public class Tomasulo {
	
	// Constantes para encapsular o tamanho de algumas estruturas.
	// ROB e RS utilizar�o apenas os �ndices de 1 a 10 e de 1 a 11,
	// respectivamente
	private final int REGSIZE = 32;
	private final int ROBSIZE = 1 + 10; // id 0 nao utilizado
	private final int RSSIZE = 1 + 11; // id 0 nao utilizado
	private final int MEMORYSIZE = 4096;
	
	// Vetor de estados dos registradores (indexados de 0 a 31)
	private Register[] RegisterStat;
	// Buffer de Reordena��o
	private BufferEntry[] ROB;
	// Esta��es de Reserva
	private ReserveStationEntry[] RS;
	// Vetor com informa��es relevantes para as previs�es de desvio
	private DetourBufferEntry[] detourBuffer;
	
	// Matrizes de output passadas para a interface gr�fica
	private String[][] RSMatrix;
	private String[][] ROBMatrix;
	private String[][] RegisterStatMatrix;
	private String[][] ExecutionDataMatrix;
	private String[][] RecentUsedMemoryMatrix;
	
	// Mem�ria de dados (4096 posi��es)
	private int[] dataMemory;
	// Mem�ria de instru��es
	private ArrayList<String> instMemory;
	// COMENTAR
	private int instCount;
	// Program Counter
	private int pc;
	
	// Guarda o �ndice das Esta��es de Reserva da �nica
	// instru��o "lw" que est� na segunda etapa de sua execu��o
	private int loadStep2 = -1;
	
	// Tipo de predi��o:
	// 1: COMENTAR
	// 2: COMENTAR
	// 3: COMENTAR
	private int predictionType;
	
	//Refer�ncia para objeto respons�vel pela interface gr�fica
	private appGUI gui;
	
	// Construtor: apenas inicializa e declara algumas vari�vels
	public Tomasulo(ArrayList<String> instructions, int type, appGUI frame){
		gui = frame; 
		predictionType = type;
		
		RegisterStat = new Register[REGSIZE];
		for (int i = 0; i < REGSIZE; i++)
			RegisterStat[i] = new Register(i);
		
		ROB = new BufferEntry[ROBSIZE];
		for (int i = 1; i < ROBSIZE; i++)
			ROB[i] = new BufferEntry(i);
		
		// A Esta��o de Reserva cont�m 5 campos Load/Store,
		// 3 campos Add e 3 campos Mult
		RS = new ReserveStationEntry[RSSIZE];
		for (int i = 1; i <= 5; i++) // 5
			RS[i] = new ReserveStationEntry(i, "Load/Store");
		for (int i = 6; i <= 8; i++) // 3
			RS[i] = new ReserveStationEntry(i, "Add");
		for (int i = 9; i < RSSIZE; i++) // 3
			RS[i] = new ReserveStationEntry(i, "Mult");
		
		// COMENTAR
		detourBuffer = new DetourBufferEntry[instructions.size()];
		for (int i = 0; i < instructions.size(); i++)
			detourBuffer[i] = new DetourBufferEntry();
		
		ROBMatrix = new String[ROBSIZE-1][6];
		RegisterStatMatrix = new String[8][12];
		RSMatrix = new String[RSSIZE-1][10];
		ExecutionDataMatrix = new String[4][2];
		RecentUsedMemoryMatrix = new String[4][2];
		
		// Campos est�ticos da matriz de output
		ExecutionDataMatrix[0][0] = "Clock Corrente";
		ExecutionDataMatrix[1][0] = "PC";
		ExecutionDataMatrix[2][0] =	"N�mero de Instru��es Contadas";
		ExecutionDataMatrix[3][0] = "Clock por Instru��o (CPI)";
		
		dataMemory = new int[MEMORYSIZE];
		instMemory = instructions;
		
		instCount = 0;
		pc = 0;
	}
	
	// M�todo utilizado apenas para formata��o do output
	private String outputFormat(int n){
		if (n == -1)
			return "";
		else
			return "" + n;
	}
	
	// Quando chamado, este m�todo atualiza as tabelas de output
	private void updateTables(){
		//Update RSMatrix
		for(int i=1; i < RSSIZE; i++){
			RSMatrix[i-1][0] = "ER" + i;
			RSMatrix[i-1][1] = RS[i].type;
			RSMatrix[i-1][2] = RS[i].busy ? "Sim" : "N�o";
			RSMatrix[i-1][3] = RS[i].instruction;
			RSMatrix[i-1][4] = "#" + outputFormat(RS[i].dest);
			RSMatrix[i-1][5] = outputFormat(RS[i].vj);
			RSMatrix[i-1][6] = outputFormat(RS[i].vk);
			RSMatrix[i-1][7] = outputFormat(RS[i].qj);
			RSMatrix[i-1][8] = outputFormat(RS[i].qk);
			RSMatrix[i-1][9] = outputFormat(RS[i].a);
		}
		
		gui.updateResStatTable(RSMatrix);
		
		for(int i=1; i< ROBSIZE; i++){
			ROBMatrix[i-1][0] = "" + ROB[i].id;
			ROBMatrix[i-1][1] = ROB[i].busy ? "Sim" : "N�o";
			ROBMatrix[i-1][2] = ROB[i].instruction + " " + ROB[i].inst_param;
			ROBMatrix[i-1][3] = ROB[i].state;
			ROBMatrix[i-1][4] = "R" + outputFormat(ROB[i].dest);
			ROBMatrix[i-1][5] = outputFormat(ROB[i].value);
		}
		
		gui.updateReordBufTable(ROBMatrix);
		
		for(int i=0; i < 8; i++){
			RegisterStatMatrix[i][0] = "R" + i;
			RegisterStatMatrix[i][3] = "R" + i + 8;
			RegisterStatMatrix[i][6] = "R" + i + 16;
			RegisterStatMatrix[i][9] = "R" + i + 2;
			RegisterStatMatrix[i][1] = "#" + outputFormat(RegisterStat[i].reorder);
			RegisterStatMatrix[i][2] = "" + RegisterStat[i].value;	
			RegisterStatMatrix[i][4] = "#" + outputFormat(RegisterStat[i+8].reorder);
			RegisterStatMatrix[i][7] = "#" + outputFormat(RegisterStat[i+16].reorder);
			RegisterStatMatrix[i][10] = "#" + outputFormat(RegisterStat[i+24].reorder);
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
	
	// Verifica se o programa j� foi encerrado. Condi��es:
	// 1. PC deve estar em um endere�o de instru��o inv�lido
	// 2. Todas as tarefas no Buffer de Reordena��o foram encerradas
	private boolean finished(){
		if (pc < instMemory.size())
			return false;
		
		for (int b = 1; b < ROBSIZE; b++)
			if (!ROB[b].state.isEmpty() && !ROB[b].state.equals("Commit"))
				return false;
		
		return true;
	}
	
	// Aqui roda o loop principal do programa
	public void run(){
		// Reinicia contador de clocks
		Timer.reiniciarContador();
		updateTables();
		
		while (!finished()){
			int start = Timer.tempoDecorrido();
			int end = Timer.tempoDecorrido();
			
			// Espera 1 clock do Timer antes de avan�ar
			while(end - start < 1){
				end = Timer.tempoDecorrido();
			}
			
			// As fases do algoritmo s�o executadas em ordem contr�ria,
			// pois assim evita-se que uma instru��o consiga passar por mais
			// de uma etapa num mesmo ciclo de clock
			consolidate();
			store();
			execute();
			if (pc < instMemory.size())
				issue();
			
			updateTables();
		}
	}

	// COMENTAR
	private void makeDetour(){
		int pcAux = pc;
		if(predictionType == 1) //Detour que sempre sup�e que segue
			pc += 1;
		else if(predictionType == 3) //Detour din�mico de 1 bit
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
		// instInfo cont�m diversas informa��es a respeito da
		// instru��o, como: nome, tipo, par�metros, etc
		ArrayList<Object> instInfo = Instructions.getInfo(inst);
		
		// Nem todos os campos estar�o preenchidos para algumas
		// instru��es (conter�o null)
		Character type = (Character)instInfo.get(0); // R, I ou J
		String name = (String)instInfo.get(1); // nome
		String param = (String)instInfo.get(2); // par�metros
		String category = (String)instInfo.get(3); // Load/Store, Add ou Mult
		Integer immediate = (Integer)instInfo.get(4); // valor imediato
		Integer address = (Integer)instInfo.get(5); // endere�o
		Integer rd = (Integer)instInfo.get(6); // rd
		Integer rs = (Integer)instInfo.get(7); // rs
		Integer rt = (Integer)instInfo.get(8); // rt
		
		// Em caso de jmp, basta atualizar o pc
		if (type == 'J'){
			pc = address;
			return;
		}
		
		// Obt�m o pr�ximo campo dispon�vel no Buffer de Reordena��o.
		// Retorna -1 se estiver cheio
		int b = nextBufferEntry();
		if (b < 0)
			return;
		
		// Obt�m o pr�ximo campo dispon�vel na Esta��o de Reserva.
		// Retorna -1 se estiver cheia
		int r = nextReserveStationEntry(category);
		if (r < 0)
			return;
		
		RS[r].clear();
		ROB[b].clear();
		
		// AN�LISE DO REGISTRADOR RS ============================
		// Se rs estiver ocupado
		if (RegisterStat[rs].busy){
			int h = RegisterStat[rs].reorder;
			
			// Sendo otimistas, podemos encontrar o valor desejado
			// no Buffer de Reordena��o caso o estado "Write" j�
			// tenha sido alcan�ado
			if (ROB[h].state.equals("Write")){
				RS[r].vj = ROB[h].value;
				RS[r].qj = 0;
			}
			// No pior caso, resta apenas registrar a depend�ncia
			else
				RS[r].qj = h;
		}
		
		// Se rs estiver livre, j� temos o valor desejado
		else{
			RS[r].vj = RegisterStat[rs].value;
			RS[r].qj = 0;
		}
		// ======================================================
		
		// AN�LISE DO REGISTRADOR RT ============================
		// Funciona de maneira an�loga ao registrador rs
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
		// ======================================================
		
		// Atualizar os campos na Esta��o de Reserva
		RS[r].busy = true;
		RS[r].dest = b;
		RS[r].instruction = name;
		
		// Atualizar os campos no Buffer de Reordena��o
		ROB[b].instruction = name;
		ROB[b].inst_param = param;
		ROB[b].state = "Issue";
		ROB[b].id = instCount + 1;
		ROB[b].pc = pc;
		ROB[b].busy = true;
		
		// Se tivermos uma instru��o do tipo R, sabemos que
		// o resultado final ser� armazenado em rd
		if (type == 'R'){
			// Atualizar registrador rd
			RegisterStat[rd].reorder = b;
			RegisterStat[rd].busy = true;
			// Guardar destino no Buffer de Reordena��o
			ROB[b].dest = rd;
		}
		
		// Se tivermos uma instru��o do tipo I, o valor
		// imediato precisa ser armazenado
		else if (type == 'I'){
			RS[r].a = immediate;
		
			// Para as instru��es "lw" e "addi", rt �
			// ser� destino do resultado
			if (name.equals("lw") || name.equals("addi")){
				// Atualizar registrador rt
				RegisterStat[rt].reorder = b;
				RegisterStat[rt].busy = true;
				// Guardar destino no Buffer de Reordena��o
				ROB[b].dest = rt;
			}
		}
		
		// � necess�rio setar o tempo necess�rio para executar
		// cada instru��o
		if (name.equals("lw"))
			RS[r].time = 4;
		else if (name.equals("sw"))
			RS[r].time = 4;
		else if (name.equals("mul"))
			RS[r].time = 3;
		else
			RS[r].time = 1;
		
		// COMENTAR
		instCount++;
		
		// Em caso de altera��o no seguimento do programa, makeDetour faz a
		// an�lise baseada no que houve no �ltimo ciclo do programa		
		if (name.equals("beq") || name.equals("ble") || name.equals("bne"))
			makeDetour();
		else
			pc++;
	}

	private void execute(){
		// Verifica inicialmente se a segunda etapa da instru��o
		// "lw" j� n�o est� sendo executada
		boolean loadStep2Free = (loadStep2 == -1);
		
		// Analisar a situa��o para cada elemento da Esta��o
		// de Reserva
		for (int r = 1; r < RSSIZE; r++){
			// Se n�o estiver ocupado, n�o nos interessa
			if (!RS[r].busy)
				continue;
			
			// Podemos pular instru��es cujo tempo de execu��o j�
			// j� zerou (execu��o conclu�da)
			if (RS[r].time <= 0)
				continue;
			
			// h � o �ndice do elemento correspondente no Buffer de
			// Reordena��o
			int h = RS[r].dest;
			
			// Caso ainda esteja no estado "Issue", a execu��o ainda
			// n�o come�ou. Precisamos verificar se as condi��es
			// para inici�-la s�o satisfeitas (a primeira delas � que
			// n�o exista mais depend�ncia em qj)
			if (ROB[h].state.equals("Issue") && RS[r].qj == 0){
				boolean startCondition;
				
				// Em caso de "sw", a instru��o precisa estar no primeiro
				// elemento do Buffer de Reordena��o
				if (RS[r].instruction.equals("sw"))
					startCondition = (h == bufferHead());
				
				// Em caso de "lw", n�o pode haver instru��es "sw" anteriores
				// ainda n�o finalizadas
				else if (RS[r].equals("lw"))
					startCondition = noStoresBefore(h);
				
				// Suspeito. Al�m disso, qk para lw n�o parece definido,
				// assim como rd para tipo R
				// COMENTAR
				else
					startCondition = (RS[r].qk == 0);
				
				if (startCondition)
					ROB[h].state = "Execute";
			}
			
			// Caso j� tenhamos iniciado a execu��o
			if (ROB[h].state.equals("Execute")){
				
				// Instru��es "lw" se dividem em duas etapas, ambas
				// de 2 clocks de dura��o. Por uma limita��o de hardware,
				// duas instru��es n�o podem estar executando a segunda
				// etapa simultaneamente 
				if (RS[r].instruction.equals("lw")){
					// Ainda na primeira etapa
					if (RS[r].time > 2){
						// Executar primeira etapa se contador chegar a 2
						if (--RS[r].time == 2)
							RS[r].a = RS[r].vj + RS[r].a;
					}
					
					// Aguardando segunda etapa iniciar
					else if (RS[r].time == 2 && loadStep2Free){
						// � necess�rio que o endere�o requisitado n�o
						// esteja sujeito a um comando "sw" anterior
						if (noStoresBeforeWithAddress(h, RS[r].a)){
							if (loadStep2 != -1){
								int b = RS[loadStep2].dest;
								// Em caso de empate, prioriade � do mais
								// mais antigo
								if (h < b)
									loadStep2 = r;
							}
							else loadStep2 = r;
						}
					}
				}
				
				// Instru��es diferentes de "lw"
				else{
					// S� atualizam RS[r].result assim que o tempo
					// chega em zero
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
					
					// Caso especial: resultado em ROB[h].a
					else if (RS[r].instruction.equals("sw"))
						ROB[h].a = RS[r].vj + RS[r].a;
					
					// BRANCHES =================================
					// � necess�rio dividir RS[r].a por 4 para as instru��es
					// de branch. Isso ocorre porque nosso pc faz contagem
					// por instru��o, e n�o por byte -> cada instru��o possui
					// 32 bits = 4 bytes
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
					// ==========================================
				}
				
			}
		}
		
		// loadStep2 != -1 se houver alguma instru��o "lw" executando
		// sua segunda etapa
		if (loadStep2 != -1){
			int r = loadStep2;
			
			// Se a etapa encerrar
			if (--RS[r].time == 0){
				int a = RS[r].a;
				RS[r].result = dataMemory[a];
				// Libera posi��o para outra instru��o "lw" que queira
				// executar sua segunda etapa
				loadStep2 = -1;
			}
		}
	}

	private void store(){
		// Durante a opera��o de store de instru��es n�o "sw",
		// apenas uma instru��o pode utilizar o barramento por
		// clock. cdb guarda o �ndice na Esta��o de Reserva da
		// instru��o que utilizar� o barramento
		int cdb = -1;
		
		// Para cada tarefa na Esta��o de Reserva
		for (int r = 1; r < RSSIZE; r++){
			// N�o estamos interassados em tarefas j� realizadas
			if (!RS[r].busy)
				continue;
			
			// S� podemos realizar a opera��o de store ap�s o t�rmino
			// da execu��o
			int h = RS[r].dest;
			if (!ROB[h].state.equals("Execute") || RS[r].time != 0)
				continue;
			
			// Caso "sw"
			if (RS[r].instruction.equals("sw")){
				// qk precisa estar livre de depend�ncia
				if (RS[r].qk == 0){
					// Atualiza o valor no Buffer de Reordena��o
					ROB[h].value = RS[r].vk;
					ROB[h].state = "Write";
					// Desocupa Esta��o de Reserva
					RS[r].busy = false;
				}
			}
			
			// Demais instru��es disputam pelo barramento. Ser� armazenada
			// em cdb a de maior prioridade (mais antiga) ap�s o for
			else{
				if (cdb != -1){
					int b = RS[cdb].dest;
					if (ROB[h].id < ROB[b].id)
						cdb = r;
				}
				else cdb = r;
			}
			
		}
		
		// Se cdb == -1, ent�o nenhuma instru��o solicitou
		// o barramento
		if (cdb != -1){
			int r = cdb;
			int h = RS[r].dest;
			
			// Por que n�o esperar qk == 0?
			
			// Desocupa Esta��o de Reserva
			RS[r].busy = false;
			
			// Atualiza os valores vj e vk de todas as
			// instru��es que dependiam do novo resultado
			// calculado
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
			
			// Atualiza Buffer de Reordena��o
			ROB[h].value = RS[r].result;
			ROB[h].state = "Write";
		}
	}
	
	private void consolidate(){
		// Apenas o primeiro elemento do Buffer de Reordena��o
		// pode ser consolidado
		int h = bufferHead();
		
		// Buffer de Reordena��o vazio
		if (h < 0)
			return;
		
		// S� faz sentido consolidar uma instru��o no estado "Write"
		if (!ROB[h].state.equals("Write"))
			return;
		
		// Destino do resultado
		int dest = ROB[h].dest;
		
		// COMENTAR
		if (ROB[h].instruction.equals("beq") ||
			ROB[h].instruction.equals("ble") ||
			ROB[h].instruction.equals("bne")){
			
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
				System.out.println("MISERAVI\n");
				
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
		
		// Caso "sw", armazenar ROB[h].value na posi��o de mem�ria calculada
		else if (ROB[h].instruction.equals("sw")){
			int a = ROB[h].a;
			dataMemory[a] = ROB[h].value;
		}
		
		// Nos demais casos, colocar no registrador "dest" o resultado
		else
			RegisterStat[dest].value = ROB[h].value;
		
		// Liberar o Buffer de Reordena��o
		ROB[h].busy = false;
		ROB[h].state = "Commit";
		// Liberar o registrador, se n�o h� mais depend�ncia
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
