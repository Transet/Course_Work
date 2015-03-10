package com.android.syncorganiser.server;


import java.io.IOException;
import java.net.ServerSocket;
import javax.swing.JButton;
import javax.swing.JFrame;
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
	
	public void run()
	{
		initframe();
	    boolean listening = true;
	
	    jta.setText("Try to listen port = "+port);
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
        jb.setText("Востановить");
        JButton jb2 = new JButton();
        jb2.setSize(120, 30);
        jb2.setText("Застановить");
        
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
