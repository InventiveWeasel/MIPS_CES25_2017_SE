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
		
		try {
			bufferInsts();	
			Tomasulo tomas = new Tomasulo(instMemory, 1, frame);
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
