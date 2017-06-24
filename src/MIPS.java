import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import GUI.appGUI;
import tomasulo.Tomasulo;

public class MIPS {
	private int[][] dataMemory = new int[4096][32];
	private ArrayList<String> instMemory = new ArrayList<String>();
	private String BENCHMARK="benchmark.txt";
	appGUI frame;
	public void run(){
		frame = new appGUI();
		frame.setVisible(true);
		
		// Obtém o tipo de predição (1, 2 ou 3) a partir de um arquivo ==================
		int predictionType = 1; // Valor default, caso não ache o arquivo
		InputStream is = null;
		try{
			is = new FileInputStream("predictionType.txt");
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			
			String type = br.readLine().substring(0, 1);
			predictionType = Integer.parseInt(type);
			System.out.println(predictionType);
		}
		catch (IOException e){
			e.printStackTrace();
		}
		// ==============================================================================
		
		try {
			bufferInsts();	
			Tomasulo tomas = new Tomasulo(instMemory, predictionType, frame);
			tomas.run();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//Lê e armazena as intruções no buffer de instruções
	private void bufferInsts() throws IOException{
		InputStream is = null;
		try {
			is = new FileInputStream(BENCHMARK);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		String inst = br.readLine().substring(0, 32);
		
		while(inst != null){
			System.out.println(inst);
			instMemory.add(inst);
			inst = br.readLine();
			if(inst!=null){
				inst = inst.substring(0, 32);
			}
		}
	}
}
