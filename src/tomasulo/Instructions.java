package tomasulo;

import java.util.ArrayList;

public abstract class Instructions {

	// tipo, nome, parametros, categoria, immediate, address, rd, rs, rt
	public static ArrayList<Object> getInfo(String inst){
		ArrayList<Object> info = new ArrayList<Object>();
		
		String opcode = inst.substring(0, 6);
		
		// Tipo J
		if (opcode.equals("000010")){
			int targetAddress = Integer.parseInt(inst.substring(6), 2);
			
			info.add('J');
			info.add("jmp");
			info.add(null);
			info.add(null);
			info.add(null);
			info.add(targetAddress);
			info.add(null);
			info.add(null);
			info.add(null);
		}
		
		else{
			int rs = Integer.parseInt(inst.substring(6, 11), 2);
			int rt = Integer.parseInt(inst.substring(11, 16), 2);
			
			// Tipo R
			if (opcode.equals("000000")){
				int rd = Integer.parseInt(inst.substring(16, 21), 2);
				String funct = inst.substring(26);
				
				String name = "";
				String parameters = "";
				String category = "";
				
				if (funct.equals("100000")){
					name = "add";
					category = "Add";
				} else if (funct.equals("011000")){
					name = "mul";
					category = "Mult";
				} else if (funct.equals("100010")){
					name = "sub";
					category = "Add";
				} else if (funct.equals("000000")){
					name = "nop";
				}
				
				parameters = "R" + rd + ",R" + rs + ",R" + rt;
				
				info.add('R');
				info.add(name);
				info.add(parameters);
				info.add(category);
				info.add(null);
				info.add(null);
				info.add(rd);
			}
			
			// Tipo I
			else{
				int immediate = Integer.parseInt(inst.substring(16), 2);
				
				String name = "";
				String parameters = "";
				String category = "";
				
				parameters = "R" + rt + ",R" + rs + "," + immediate;
				
				if (opcode.equals("001000")){
					name = "addi";
					category = "Add";
				} else if (opcode.equals("000101")){
					name = "beq";
					category = "Add";
				} else if (opcode.equals("000111")){
					name = "ble";
					category = "Add";
				} else if (opcode.equals("000100")){
					name = "bne";
					category = "Add";
				} else{
					parameters = "R" + rt + "," + immediate + "(R" + rs + ")";
					
					if (opcode.equals("100011")){
						name = "lw";
						category = "Load/Store";
					} else if (opcode.equals("101011")){
						name = "sw";
						category = "Load/Store";
					}
				}
				
				info.add('I');
				info.add(name);
				info.add(parameters);
				info.add(category);
				info.add(immediate);
				info.add(null);
				info.add(null);
			}
			
			info.add(rs);
			info.add(rt);
		}
		
		return info;
	}
	
}
