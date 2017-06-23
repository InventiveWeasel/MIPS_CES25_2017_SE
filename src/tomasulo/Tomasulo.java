package tomasulo;
import GUI.appGUI;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Tomasulo {
	
	// Constantes para encapsular o tamanho de algumas estruturas.
	// ROB e RS utilizarão apenas os índices de 1 a 10 e de 1 a 11,
	// respectivamente
	private final int REGSIZE = 32;
	private final int ROBSIZE = 1 + 10; // id 0 nao utilizado
	private final int RSSIZE = 1 + 11; // id 0 nao utilizado
	private final int MEMORYSIZE = 4096;
	
	// Vetor de estados dos registradores (indexados de 0 a 31)
	private Register[] RegisterStat;
	// Buffer de Reordenação
	private BufferEntry[] ROB;
	// Estações de Reserva
	private ReserveStationEntry[] RS;
	// Vetor com informações relevantes para as previsões de desvio
	private DetourBufferEntry[] detourBuffer;
	
	// Matrizes de output passadas para a interface gráfica
	private String[][] RSMatrix;
	private String[][] ROBMatrix;
	private String[][] RegisterStatMatrix;
	private String[][] ExecutionDataMatrix;
	private String[][] RecentUsedMemoryMatrix;
	
	// Memória de dados (4096 posições)
	private int[] dataMemory;
	// Memória de instruções
	private ArrayList<String> instMemory;
	// Contador de ids para o Buffer de Reordenação
	private int bufferId;
	// Conta quantas instruções foram iniciadas pelo simulador
	private int instCount;
	// Contador de clocks
	private int clockCount;
	// Program Counter
	private int pc;
	
	// Guarda o índice das Estações de Reserva da única
	// instrução "lw" que está na segunda etapa de sua execução
	private int loadStep2 = -1;
	
	// Tipo de predição:
	// 1: COMENTAR
	// 2: COMENTAR
	// 3: COMENTAR
	private int predictionType;
	
	//Referência para objeto responsável pela interface gráfica
	private appGUI gui;
	
	// Construtor: apenas inicializa e declara algumas variávels
	public Tomasulo(ArrayList<String> instructions, int type, appGUI frame){
		gui = frame; 
		predictionType = type;
		
		RegisterStat = new Register[REGSIZE];
		for (int i = 0; i < REGSIZE; i++)
			RegisterStat[i] = new Register(i);
		
		ROB = new BufferEntry[ROBSIZE];
		for (int i = 1; i < ROBSIZE; i++)
			ROB[i] = new BufferEntry(i);
		
		// A Estação de Reserva contém 5 campos Load/Store,
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
		
		// Campos estáticos da matriz de output
		ExecutionDataMatrix[0][0] = "Clock Corrente";
		ExecutionDataMatrix[1][0] = "PC";
		ExecutionDataMatrix[2][0] =	"Número de Instruções Contadas";
		ExecutionDataMatrix[3][0] = "Clock por Instrução (CPI)";
		
		dataMemory = new int[MEMORYSIZE];
		instMemory = instructions;
		
		bufferId = 0;
		instCount = 0;
		clockCount = 0;
		pc = 0;
	}
	
	// Método utilizado apenas para formatação do output
	private String outputFormat(int n){
		if (n == -1)
			return "";
		else
			return "" + n;
	}
	
	// Método utilizado apenas para formatação do output
	private String outputFormat2(int dest){
		if (dest == -1)
			return "";
		else
			return "#" + ROB[dest].id;
	}
	
	// Quando chamado, este método atualiza as tabelas de output
	private void updateTables(){
		DecimalFormat df = new DecimalFormat("0.000");
		
		//Update RSMatrix
		for(int i=1; i < RSSIZE; i++){
			RSMatrix[i-1][0] = "ER" + i;
			RSMatrix[i-1][1] = RS[i].type;
			RSMatrix[i-1][2] = RS[i].busy ? "Sim" : "Não";
			RSMatrix[i-1][3] = RS[i].instruction;
			RSMatrix[i-1][4] = outputFormat2(RS[i].dest);
			RSMatrix[i-1][5] = outputFormat(RS[i].vj);
			RSMatrix[i-1][6] = outputFormat(RS[i].vk);
			RSMatrix[i-1][7] = outputFormat(RS[i].qj);
			RSMatrix[i-1][8] = outputFormat(RS[i].qk);
			RSMatrix[i-1][9] = outputFormat(RS[i].a);
		}
		
		gui.updateResStatTable(RSMatrix);
		
		for(int i=1; i< ROBSIZE; i++){
			ROBMatrix[i-1][0] = "" + ROB[i].id;
			ROBMatrix[i-1][1] = ROB[i].busy ? "Sim" : "Não";
			ROBMatrix[i-1][2] = ROB[i].instruction + " " + ROB[i].inst_param;
			ROBMatrix[i-1][3] = ROB[i].state;
			ROBMatrix[i-1][4] = "R" + outputFormat(ROB[i].dest);
			ROBMatrix[i-1][5] = outputFormat(ROB[i].value);
		}
		
		gui.updateReordBufTable(ROBMatrix);
		
		for(int i=0; i < 8; i++){
			RegisterStatMatrix[i][0] = "R" + i;
			RegisterStatMatrix[i][3] = "R" + (i + 8);
			RegisterStatMatrix[i][6] = "R" + (i + 16);
			RegisterStatMatrix[i][9] = "R" + (i + 2);
			RegisterStatMatrix[i][1] = outputFormat2(RegisterStat[i].reorder);
			RegisterStatMatrix[i][2] = "" + RegisterStat[i].value;	
			RegisterStatMatrix[i][4] = outputFormat2(RegisterStat[i+8].reorder);
			RegisterStatMatrix[i][7] = outputFormat2(RegisterStat[i+16].reorder);
			RegisterStatMatrix[i][10] = outputFormat2(RegisterStat[i+24].reorder);
			RegisterStatMatrix[i][5] = "" + RegisterStat[i+8].value;
			RegisterStatMatrix[i][8] = "" + RegisterStat[i+16].value;
			RegisterStatMatrix[i][11] = "" + RegisterStat[i+24].value;
		}
		
		gui.updateRegsTable(RegisterStatMatrix);
		
		ExecutionDataMatrix[0][1] = "" + clockCount;
		ExecutionDataMatrix[1][1] =	"" + pc;
		ExecutionDataMatrix[2][1] =	"" + instCount;
		if(Timer.tempoDecorrido() == 0)
			ExecutionDataMatrix[3][1] = ""; 
		else
			ExecutionDataMatrix[3][1] =	"" + df.format(instCount * 1.0 / clockCount);
		
		gui.updateExecTable(ExecutionDataMatrix);
		
		/*RecentUsedMemoryMatrix[3][0] = RecentUsedMemoryMatrix[2][0];
		RecentUsedMemoryMatrix[2][0] = RecentUsedMemoryMatrix[1][0];
		RecentUsedMemoryMatrix[1][0] = RecentUsedMemoryMatrix[0][0];
		RecentUsedMemoryMatrix[0][0] = ;*/
		
	}
	
	// Verifica se o programa já foi encerrado. Condições:
	// 1. PC deve estar em um endereço de instrução inválido
	// 2. Todas as tarefas no Buffer de Reordenação foram encerradas
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
			
			// Espera 1 clock do Timer antes de avançar
			while(end - start < 1){
				end = Timer.tempoDecorrido();
			}
			
			// As fases do algoritmo são executadas em ordem contrária,
			// pois assim evita-se que uma instrução consiga passar por mais
			// de uma etapa num mesmo ciclo de clock
			consolidate();
			store();
			execute();
			if (pc < instMemory.size())
				issue();
			
			clockCount++;
			updateTables();
		}
	}

	// COMENTAR
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
		// instInfo contém diversas informações a respeito da
		// instrução, como: nome, tipo, parâmetros, etc
		ArrayList<Object> instInfo = Instructions.getInfo(inst);
		
		// Nem todos os campos estarão preenchidos para algumas
		// instruções (conterão null)
		Character type = (Character)instInfo.get(0); // R, I ou J
		String name = (String)instInfo.get(1); // nome
		String param = (String)instInfo.get(2); // parâmetros
		String category = (String)instInfo.get(3); // Load/Store, Add ou Mult
		Integer immediate = (Integer)instInfo.get(4); // valor imediato
		Integer address = (Integer)instInfo.get(5); // endereço
		Integer rd = (Integer)instInfo.get(6); // rd
		Integer rs = (Integer)instInfo.get(7); // rs
		Integer rt = (Integer)instInfo.get(8); // rt
		
		// Em caso de jmp, basta atualizar o pc
		if (type == 'J'){
			pc = address;
			instCount++;
			return;
		}
		
		// Obtém o próximo campo disponível no Buffer de Reordenação.
		// Retorna -1 se estiver cheio
		int b = nextBufferEntry();
		if (b < 0)
			return;
		
		// Obtém o próximo campo disponível na Estação de Reserva.
		// Retorna -1 se estiver cheia
		int r = nextReserveStationEntry(category);
		if (r < 0)
			return;
		
		RS[r].clear();
		ROB[b].clear();
		
		// ANÁLISE DO REGISTRADOR RS ============================
		// Se rs estiver ocupado
		if (RegisterStat[rs].busy){
			int h = RegisterStat[rs].reorder;
			
			// Sendo otimistas, podemos encontrar o valor desejado
			// no Buffer de Reordenação caso o estado "Write" já
			// tenha sido alcançado
			if (ROB[h].state.equals("Write")){
				RS[r].vj = ROB[h].value;
				RS[r].qj = 0;
			}
			// No pior caso, resta apenas registrar a dependência
			else
				RS[r].qj = h;
		}
		
		// Se rs estiver livre, já temos o valor desejado
		else{
			RS[r].vj = RegisterStat[rs].value;
			RS[r].qj = 0;
		}
		// ======================================================
		
		// ANÁLISE DO REGISTRADOR RT ============================
		// Funciona de maneira análoga ao registrador rs
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
		
		// Atualizar os campos na Estação de Reserva
		RS[r].busy = true;
		RS[r].dest = b;
		RS[r].instruction = name;
		
		// Atualizar os campos no Buffer de Reordenação
		ROB[b].instruction = name;
		ROB[b].inst_param = param;
		ROB[b].state = "Issue";
		ROB[b].id = bufferId + 1;
		ROB[b].pc = pc;
		ROB[b].busy = true;
		
		// Se tivermos uma instrução do tipo R, sabemos que
		// o resultado final será armazenado em rd
		if (type == 'R'){
			// Atualizar registrador rd
			RegisterStat[rd].reorder = b;
			RegisterStat[rd].busy = true;
			// Guardar destino no Buffer de Reordenação
			ROB[b].dest = rd;
		}
		
		// Se tivermos uma instrução do tipo I, o valor
		// imediato precisa ser armazenado
		else if (type == 'I'){
			RS[r].a = immediate;
		
			// Para as instruções "lw" e "addi", rt é
			// será destino do resultado
			if (name.equals("lw") || name.equals("addi")){
				// Atualizar registrador rt
				RegisterStat[rt].reorder = b;
				RegisterStat[rt].busy = true;
				// Guardar destino no Buffer de Reordenação
				ROB[b].dest = rt;
			}
		}
		
		// É necessário setar o tempo necessário para executar
		// cada instrução
		if (name.equals("lw"))
			RS[r].time = 4;
		else if (name.equals("sw"))
			RS[r].time = 4;
		else if (name.equals("mul"))
			RS[r].time = 3;
		else
			RS[r].time = 1;
		
		// Dado que um novo elemento foi adicionado ao Buffer de Reordenação,
		// precisamos incrementar o contador de ids
		bufferId++;
		// E, obviamente, o contador de instruções
		instCount++;
		
		// Em caso de alteração no seguimento do programa, makeDetour faz a
		// análise baseada no que houve no último ciclo do programa		
		if (name.equals("beq") || name.equals("ble") || name.equals("bne"))
			makeDetour();
		else
			pc++;
	}

	private void execute(){
		// Verifica inicialmente se a segunda etapa da instrução
		// "lw" já não está sendo executada
		boolean loadStep2Free = (loadStep2 == -1);
		
		// Analisar a situação para cada elemento da Estação
		// de Reserva
		for (int r = 1; r < RSSIZE; r++){
			// Se não estiver ocupado, não nos interessa
			if (!RS[r].busy)
				continue;
			
			// Podemos pular instruções cujo tempo de execução já
			// já zerou (execução concluída)
			if (RS[r].time <= 0)
				continue;
			
			// h é o índice do elemento correspondente no Buffer de
			// Reordenação
			int h = RS[r].dest;
			
			// Caso ainda esteja no estado "Issue", a execução ainda
			// não começou. Precisamos verificar se as condições
			// para iniciá-la são satisfeitas (a primeira delas é que
			// não exista mais dependência em qj)
			if (ROB[h].state.equals("Issue") && RS[r].qj == 0){
				boolean startCondition;
				
				// Em caso de "sw", a instrução precisa estar no primeiro
				// elemento do Buffer de Reordenação
				if (RS[r].instruction.equals("sw"))
					startCondition = (h == bufferHead());
				
				// Em caso de "lw", não pode haver instruções "sw" anteriores
				// ainda não finalizadas
				else if (RS[r].equals("lw"))
					startCondition = noStoresBefore(h);
				
				// Suspeito. Além disso, qk para lw não parece definido,
				// assim como rd para tipo R
				// COMENTAR
				else
					startCondition = (RS[r].qk == 0);
				
				if (startCondition)
					ROB[h].state = "Execute";
			}
			
			// Caso já tenhamos iniciado a execução
			if (ROB[h].state.equals("Execute")){
				
				// Instruções "lw" se dividem em duas etapas, ambas
				// de 2 clocks de duração. Por uma limitação de hardware,
				// duas instruções não podem estar executando a segunda
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
						// É necessário que o endereço requisitado não
						// esteja sujeito a um comando "sw" anterior
						if (noStoresBeforeWithAddress(h, RS[r].a)){
							if (loadStep2 != -1){
								int b = RS[loadStep2].dest;
								// Em caso de empate, prioriade é do mais
								// mais antigo
								if (h < b)
									loadStep2 = r;
							}
							else loadStep2 = r;
						}
					}
				}
				
				// Instruções diferentes de "lw"
				else{
					// Só atualizam RS[r].result assim que o tempo
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
					// É necessário dividir RS[r].a por 4 para as instruções
					// de branch. Isso ocorre porque nosso pc faz contagem
					// por instrução, e não por byte -> cada instrução possui
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
		
		// loadStep2 != -1 se houver alguma instrução "lw" executando
		// sua segunda etapa
		if (loadStep2 != -1){
			int r = loadStep2;
			
			// Se a etapa encerrar
			if (--RS[r].time == 0){
				int a = RS[r].a;
				RS[r].result = dataMemory[a];
				// Libera posição para outra instrução "lw" que queira
				// executar sua segunda etapa
				loadStep2 = -1;
			}
		}
	}

	private void store(){
		// Durante a operação de store de instruções não "sw",
		// apenas uma instrução pode utilizar o barramento por
		// clock. cdb guarda o índice na Estação de Reserva da
		// instrução que utilizará o barramento
		int cdb = -1;
		
		// Para cada tarefa na Estação de Reserva
		for (int r = 1; r < RSSIZE; r++){
			// Não estamos interassados em tarefas já realizadas
			if (!RS[r].busy)
				continue;
			
			// Só podemos realizar a operação de store após o término
			// da execução
			int h = RS[r].dest;
			if (!ROB[h].state.equals("Execute") || RS[r].time != 0)
				continue;
			
			// Caso "sw"
			if (RS[r].instruction.equals("sw")){
				// qk precisa estar livre de dependência
				if (RS[r].qk == 0){
					// Atualiza o valor no Buffer de Reordenação
					ROB[h].value = RS[r].vk;
					ROB[h].state = "Write";
					// Desocupa Estação de Reserva
					RS[r].busy = false;
				}
			}
			
			// Demais instruções disputam pelo barramento. Será armazenada
			// em cdb a de maior prioridade (mais antiga) após o for
			else{
				if (cdb != -1){
					int b = RS[cdb].dest;
					if (ROB[h].id < ROB[b].id)
						cdb = r;
				}
				else cdb = r;
			}
			
		}
		
		// Se cdb == -1, então nenhuma instrução solicitou
		// o barramento
		if (cdb != -1){
			int r = cdb;
			int h = RS[r].dest;
			
			// Por que não esperar qk == 0?
			
			// Desocupa Estação de Reserva
			RS[r].busy = false;
			
			// Atualiza os valores vj e vk de todas as
			// instruções que dependiam do novo resultado
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
			
			// Atualiza Buffer de Reordenação
			ROB[h].value = RS[r].result;
			ROB[h].state = "Write";
		}
	}
	
	private void consolidate(){
		// Apenas o primeiro elemento do Buffer de Reordenação
		// pode ser consolidado
		int h = bufferHead();
		
		// Buffer de Reordenação vazio
		if (h < 0)
			return;
		
		// Só faz sentido consolidar uma instrução no estado "Write"
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
					
				bufferId = ROB[h].id;
				
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
		
		// Caso "sw", armazenar ROB[h].value na posição de memória calculada
		else if (ROB[h].instruction.equals("sw")){
			int a = ROB[h].a;
			dataMemory[a] = ROB[h].value;
		}
		
		// Nos demais casos, colocar no registrador "dest" o resultado
		else
			RegisterStat[dest].value = ROB[h].value;
		
		// Liberar o Buffer de Reordenação
		ROB[h].busy = false;
		ROB[h].state = "Commit";
		// Liberar o registrador, se não há mais dependência
		if (dest >= 0 && RegisterStat[dest].reorder == h)
			RegisterStat[dest].busy = false;
	}
	
	// FUNÇÕES AUXILIARES =======================================
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
		int id = bufferId % (ROBSIZE - 1) + 1;
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
	// ==========================================================
}
