import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class View{
	int HEIGHT = 500;
	int WIDTH = 650;
	String[] resStatCols={"ID", "Tipo","Busy","Instrução","Dest","Vj","Vk","Qj","Qk","A"};
	Object [][] resStatData = {
        {new Integer(43),"1","sim","lw","43","1","0","1","0","23"}};
	
	String[] memRecUsedCols={"Endereço","Valor"};
	Object [][] memRecUsedData = {
        {new Integer(24),"?"},
        {new Integer(28),"?"},
        {new Integer(32),"?"},
        {new Integer(36),"?"}
        };
	
	String[] reordBufCols = {"Entrada","Ocupado","Instrução","Estado","Destino","Valor"};
	Object[][] reordBufData = {
			{"","","","","",""}
	};
	
	String[] execCols={"",""};
	Object[][] execData={
			{"Clock Corrente:", "?"},
			{"PC:", "?"},
			{"Número de instruções concluídas:", "?"},
			{"Clock por Instrução (CPI):", "?"}
	};
	String[] regsCols={"","Qi","Vi","","Qi","Vi","","Qi","Vi","","Qi","Vi"};
	Object[][] regsData={
			{"R0", "0","0","R8", "?","?","R16", "?","?","R24", "?","?"},
			{"R1", "0","0","R9", "?","?","R17", "?","?","R25", "?","?"},
			{"R2", "0","0","R10", "?","?","R18", "?","?","R26", "?","?"},
			{"R3", "0","0","R11", "?","?","R19", "?","?","R27", "?","?"},
			{"R4", "0","0","R12", "?","?","R20", "?","?","R28", "?","?"},
			{"R5", "0","0","R13", "?","?","R21", "?","?","R29", "?","?"},
			{"R6", "0","0","R14", "?","?","R22", "?","?","R30", "?","?"},
			{"R7", "0","0","R15", "?","?","R23", "?","?","R31", "?","?"},
	};
	
	DefaultTableModel model;
	JButton play,fastforward,pause;
	JPanel pane, pane2, paneButtons;
	JScrollPane scrollPane,scrollPane2,scrollPane3,scrollPane4,scrollPane5;
	JTable resStat,memRecUsed,reordBuf,exec,regs;
	JFrame f1,f2,f3,f4,f5,f6;
	
	public static void main(String[] args){
		View v = new View();
		v.createGUI();
	}

	public void createGUI(){
		
		//Tabela de Estações de Reserva
		resStat = new JTable(resStatData,resStatCols);
		pane = new JPanel();
		scrollPane = new JScrollPane(resStat);
		//f = new JFrame();
		f1 = new JFrame();
		f1.add(scrollPane);
		f1.pack();
		f1.setVisible(true);
		f1.setTitle("Estações de Reserva");
		
		//Botões de Controle
		paneButtons = new JPanel();
		paneButtons.setLayout(new BoxLayout(paneButtons,BoxLayout.LINE_AXIS));
		play = new JButton("Play");
		pause = new JButton("Pause");
		fastforward = new JButton("Fast");
		paneButtons.add(play);
		paneButtons.add(pause);
		paneButtons.add(fastforward);
		f2 = new JFrame();
		f2.add(paneButtons);
		f2.setVisible(true);
		f2.pack();
		f2.setTitle("Botões de Controle");
		
		
		//Tabela de Memória recentemente usada
		memRecUsed = new JTable(memRecUsedData, memRecUsedCols);
		scrollPane2 = new JScrollPane(memRecUsed);
		//pane2.add(memRecUsed);
		f3 = new JFrame();
		f3.add(scrollPane2);
		f3.setVisible(true);
		f3.pack();
		f3.setTitle("Memória Recente Usada");
		
		//Tabela Buffer de Reordenação
		reordBuf = new JTable(reordBufData,reordBufCols);
		scrollPane3 = new JScrollPane(reordBuf);
		f4 = new JFrame();
		f4.add(scrollPane3);
		f4.setVisible(true);
		f4.pack();
		f4.setTitle("Buffer de Reordenação");
		
		//Dados de execução
		exec = new JTable(execData,execCols);
		scrollPane4 = new JScrollPane(exec);
		f5 = new JFrame();
		f5.add(scrollPane4);
		f5.setVisible(true);
		f5.pack();
		f5.setTitle("Dados de execução");
		
		//Registradores
		model = new DefaultTableModel(regsData,regsCols);
		regs = new JTable(model);
		scrollPane5 = new JScrollPane(regs);
		f6 = new JFrame();
		f6.add(scrollPane5);
		f6.setVisible(true);
		f6.pack();
		f6.setTitle("Registradores");
		model.setValueAt("Ha!", 6, 7);
		
		
		//f.add(pane);
		//pane.add(scrollPane);
		//pane.add(pane2);
		//f.setVisible(true);
		//f.setSize(WIDTH, HEIGHT);
		
		
		
		
		//f.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
}
