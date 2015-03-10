package com.android.syncorganiser.server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Gui extends Thread {

    private JTextArea area;
    private JFrame f;
    private JButton jbSend;
    private JButton jbRecive;
    private JPanel panelf;
    private JScrollPane scrollBar;
    
    private final int FHeight = 500;
    private final int FWidth = 400;
    
    private Server _sv;
    
    private boolean isInterrupted = false;
    
    public void getSrv(Server sv) {
    	_sv = sv;
    }

    public void run() {
    	while(isInterrupted == false) {
    		Thread.yield();
    	}
    }
    
	public int init(){	  	
    	try {
	    	f = new JFrame("Server");
	        area = new JTextArea();
	        jbSend = new JButton();
	        jbRecive = new JButton();
	        panelf = new JPanel();
	        
	        scrollBar = new JScrollPane(area);
	        scrollBar.setAutoscrolls(true);
	        f.getContentPane().add(scrollBar);

	        panelf.setLayout(null); 

	        jbRecive.setText("Recive");
	        jbRecive.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					//Запуск ImportFile.java Принятие файла
					_sv.serverPortSender.Recive();
				}
			});
	        
	        jbSend.setText("Send");
	        jbSend.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					//Запуск ExportFile.java Отправка файла
					_sv.serverPortSender.Send();
				}
			});
	        
	        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	       
	        f.addWindowListener(new WindowAdapter() {
	        	 
	            @Override
	            public void windowClosing(WindowEvent we) {
	                String ObjButtons[] = {"Yes", "No"};
	                int PromptResult = JOptionPane.showOptionDialog(null,
	                        "Are you sure you want to exit?", "Exit",
	                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
	                        ObjButtons, ObjButtons[1]);
	                if (PromptResult == 0) {
	                	isInterrupted = true;
	                    _sv.exitFromGui();
	                    f.dispose();
	                }
	            }
	        });
	        
	        f.setSize(FWidth, FHeight);
	        
	        area.setEditable(false);
	        area.setLineWrap(true);
	        
	        jbSend.setBounds(FWidth/2+((FWidth/2-120)/2), FHeight-70, 120, 30);
	        jbRecive.setBounds((FWidth/2-120)/2, FHeight-70, 120, 30);
	        area.setBounds(FWidth/8, 15,300, 300);
	        
	        panelf.add(area);
	        panelf.add(jbRecive);
	        panelf.add(jbSend);


	        f.getContentPane().add(panelf);
	        
	        f.setResizable(false);
	        //Окно расположит по центру экрана.
	        f.setLocationRelativeTo(null);	
	        f.setVisible(true); 
    	} catch(Exception ex) {return 1; }
    	return 0;
    }
    
    
    protected void appendText(String text) {
    	if(area != null)
    		area.append(text);
    }
    
    protected void setText(String text) {
    	if(area != null)
    		area.setText(text);
    }
    
}
