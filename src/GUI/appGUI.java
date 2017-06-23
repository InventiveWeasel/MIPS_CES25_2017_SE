package GUI;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Box;
import java.awt.Dimension;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Color;

public class appGUI extends JFrame {

	private JPanel contentPane;
	private JTable table;
	private JTable table_1;
	private JTable table_2;
	private JTable table_3;
	private JTable table_4;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					appGUI frame = new appGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public appGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 926, 582);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Esta\u00E7\u00F5es de Reserva", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_4.setBounds(37, 23, 580, 226);
		contentPane.add(panel_4);
		panel_4.setLayout(null);
		//contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 16, 564, 203);
		panel_4.add(scrollPane);
		
		table = new JTable();
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{"ER1", "Load/Store", null, null, null, null, null, null, null, null},
				{"ER2", "Load/Store", null, null, null, null, null, null, null, null},
				{"ER3", "Load/Store", null, null, null, null, null, null, null, null},
				{"ER4", "Load/Store", null, null, null, null, null, null, null, null},
				{"ER5", "Load/Store", null, null, null, null, null, null, null, null},
				{"ER6", "Add", null, null, null, null, null, null, null, null},
				{"ER7", "Add", null, null, null, null, null, null, null, null},
				{"ER8", "Add", null, null, null, null, null, null, null, null},
				{"ER9", "Mult", null, null, null, null, null, null, null, null},
				{"ER10", "Mult", null, null, null, null, null, null, null, null},
				{"ER11", "Mult", null, null, null, null, null, null, null, null},
			},
			new String[] {
				"ID", "Tipo", "Busy", "Instru\u00E7\u00E3o", "Dest", "Vj", "Vk", "Qj", "Qk", "A"
			}
		));
		table.getColumnModel().getColumn(0).setPreferredWidth(41);
		scrollPane.setViewportView(table);
		
		JPanel panel = new JPanel();
		panel.setBounds(631, 23, 269, 30);
		contentPane.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JButton btnNewButton = new JButton("Play");
		panel_1.add(btnNewButton);
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JButton btnNewButton_1 = new JButton("FastForward");
		panel_2.add(btnNewButton_1);
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JButton btnPause = new JButton("Pause");
		panel_3.add(btnPause);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Mem\u00F3ria Recente Usada", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_5.setBounds(627, 64, 244, 114);
		contentPane.add(panel_5);
		panel_5.setLayout(null);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(6, 16, 228, 91);
		panel_5.add(scrollPane_1);
		
		table_1 = new JTable();
		table_1.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null},
				{null, null},
				{null, null},
				{null, null},
			},
			new String[] {
				"Endere\u00E7o", "Valor"
			}
		));
		scrollPane_1.setViewportView(table_1);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Dados de Execu\u00E7\u00E3o", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_6.setBounds(627, 178, 244, 117);
		contentPane.add(panel_6);
		panel_6.setLayout(null);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(6, 16, 232, 94);
		panel_6.add(scrollPane_2);
		
		table_2 = new JTable();
		table_2.setModel(new DefaultTableModel(
			new Object[][] {
				{"Clock Corrente:", "?"},
				{"PC:", "?"},
				{"N\u00FAmero de Instru\u00E7\u00F5es Conclu\u00EDdas:", "?"},
				{"Clock por Instru\u00E7\u00E3o (CPI)", "?"},
			},
			new String[] {
				"Grandeza", "Valor"
			}
		));
		table_2.getColumnModel().getColumn(0).setPreferredWidth(187);
		scrollPane_2.setViewportView(table_2);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Buffer de Reordena\u00E7\u00E3o", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_7.setBounds(37, 298, 435, 212);
		contentPane.add(panel_7);
		panel_7.setLayout(null);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(6, 16, 423, 189);
		panel_7.add(scrollPane_3);
		
		table_3 = new JTable();
		table_3.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
				{null, null, null, null, null, null},
			},
			new String[] {
				"Entrada", "Ocupado", "Instru\u00E7\u00E3o", "Estado", "Destino", "Valor"
			}
		));
		scrollPane_3.setViewportView(table_3);
		
		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Registradores", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_8.setBounds(555, 308, 316, 180);
		contentPane.add(panel_8);
		panel_8.setLayout(null);
		
		JScrollPane scrollPane_4 = new JScrollPane();
		scrollPane_4.setBounds(6, 16, 304, 157);
		panel_8.add(scrollPane_4);
		
		table_4 = new JTable();
		table_4.setModel(new DefaultTableModel(
			new Object[][] {
				{"R0", "0", "0", "R8", "0", "?", "R16", "0", "?", "R24", "0", "?"},
				{"R1", "ER1", "?", "R9", "0", "?", "R17", "0", "?", "R25", "0", "?"},
				{"R2", "0", "?", "R10", "0", "?", "R18", "0", "?", "R26", "0", "?"},
				{"R3", "0", "?", "R11", "0", "?", "R19", "0", "?", "R27", "0", "?"},
				{"R4", "0", "?", "R12", "0", "?", "R20", "0", "?", "R28", "0", "?"},
				{"R5", "0", "?", "R13", "0", "?", "R21", "0", "?", "R29", "0", "?"},
				{"R6", "0", "?", "R14", "0", "?", "R22", "0", "?", "R30", "0", "?"},
				{"R7", "0", "?", "R15", "0", "?", "R23", "0", "?", "R31", "0", "?"},
			},
			new String[] {
				"", "Qi", "Vi", "", "Qi", "Vi", "", "Qi", "Vi", "", "Qi", "Vi"
			}
		));
		scrollPane_4.setViewportView(table_4);
	}
}