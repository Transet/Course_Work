package com.android.syncorganiser.server;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class MultiServer extends Thread {

	ServerSocket serverSocket = null;
	int port = 7172;
	
	private JFrame jf;
	private JTextArea jta;
	private JPanel paneljf;
    private final int FHeight = 500;
    private final int FWidth = 400;
	private JFrame frame;
	private final String IPV4_PATTERN = 
	        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    
	public void run()
	{
		initframe();
	    boolean listening = true;
	
	    //jta.setText("Try to listen port = "+port);
	    jta.setText("Press \"Connect\" on phone");
	    try {
	        serverSocket = new ServerSocket(port);
	    } catch (IOException e) {
	        System.err.println("Could not listen on port: " + port);
	        System.exit(-1);
	    }
	
	    while (listening)
	    {
	        try {
	        	
				new Server(serverSocket.accept()).start();
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				jta.setText(e.toString());
			}
	        //System.out.println("Started: " + serverSocket);
	    }
	
	    try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    jta.setText("Stoped... ");
	    jf.setVisible(false);
	}
	
	private void checkIP(String s) {
		 	String ip = s;
	        @SuppressWarnings("unused")
			String pingResult = "";

	        String pingCmd = "ping " + ip;
	        try {
	            Runtime r = Runtime.getRuntime();
	            Process p = r.exec(pingCmd);

	            BufferedReader in = new BufferedReader(new
	            InputStreamReader(p.getInputStream()));
	            String inputLine;
	            while ((inputLine = in.readLine()) != null) {
	                System.out.println(inputLine);
	                pingResult += inputLine;
	            }
	            in.close();

	        } catch (IOException e) {
	            System.out.println(e);
	        }

	}
	
	private void initframe() {
		jf = new JFrame("Server");
        jta = new JTextArea(); 
        paneljf = new JPanel();
        //Построчное расположение (new FlowLayout()) or null(Если позиционировать по абсолютным кординатам
        paneljf.setLayout(null); 
        
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(FWidth, FHeight);
        
        jta.setEditable(false);
        
        JButton jb = new JButton();
        jb.setSize(120, 30);
        jb.setText("Закрыть");
        jb.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
        JButton jb2 = new JButton();
        jb2.setSize(120, 30);
        jb2.setText("Проверка");
        jb2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String s = (String)JOptionPane.showInputDialog(
	                    frame,
	                    "Введите IP:\n"
	                    + "\"IP найдете на телефоне...\"",
	                    "Check IP",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    null,
	                    "127.0.0.1");

				//If a string was returned, say so.
				 Pattern p = Pattern.compile(IPV4_PATTERN);  
			     Matcher m = p.matcher(s);  
			     boolean b =  m.matches();  
				if ((s != null) && (s.length() > 0) && b) {
				    jta.setText("BACON!!!");
				    checkIP(s);
				    return;
				}
			
				//If you're here, the return value was null/empty.
				jta.setText("Вы ввели не правильный IP Адресс...");
			}
		});
        
        jb.setBounds(FWidth/2+((FWidth/2-120)/2), FHeight-70, 120, 30);
        jb2.setBounds((FWidth/2-120)/2, FHeight-70, 120, 30);
        jta.setBounds(FWidth/4, FHeight/10,200, 15);
        
        paneljf.add(jb);
        paneljf.add(jb2);
        paneljf.add(jta);
        
        jf.getContentPane().add(paneljf);
        
        //Окно расположит по центру экрана.
        jf.setLocationRelativeTo(null);	
        jf.setVisible(true); 
	}
	
}
