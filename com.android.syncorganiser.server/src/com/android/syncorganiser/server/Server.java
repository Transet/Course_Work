package com.android.syncorganiser.server;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Server extends Thread{
	
	private Socket serverSocket = null; //���������� �����
	
	protected OutputStream outF = null;
	protected InputStream inF = null;
	
	protected ServerPortSender serverPortSender = null;
	protected ServerPortListner serverPortListner = null;
	
	private String pathToFile;
	private String file = "contact.syf";
	
	private boolean isRun = true;
	//��������������� ��� ������� �����������: (new Server()).start();
	public Server(Socket accept) {
		// TODO Auto-generated constructor stub
		this.serverSocket = accept;
	}

	protected Gui gui;

	public void run() { 
		initSock();
		initServerPortLS();
		gui = new Gui();
		gui.start();
		gui.getSrv(this);
		if(gui.init()==1) { 
			JFrame fr = new JFrame();
			JOptionPane.showMessageDialog(fr,"������ � ������������� ����������.","Fatal error",JOptionPane.ERROR_MESSAGE);
			while(fr.isVisible()){}
			return;
		}
		gui.setText("Connection accepted: " + serverSocket);//����� ������
		getPath();
		while(isRun) 
		{
			Thread.yield();
			//��������� ���� �� �������
		}
		System.out.println("����� �� Server.java");
		exitThread();
	}

	private void exitThread() {
		gui = null;
		try {
			inF.close();
			outF.close();
			serverSocket.close();
		}
		catch(IOException e) {
			System.out.println("������ � Server::exitThread() IOException");
		} catch(NullPointerException e) {
			System.out.println("������ � Server::exitThread() NullPointerException");
		} finally {
			System.exit(0);
		}
	}
	
	private void getPath() {
		String tmp = "";
		try {
			tmp = new String(java.net.URLDecoder.decode(ClassLoader.getSystemClassLoader().getResource(".").getPath(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pathToFile = tmp.substring(1, tmp.length());
		gui.appendText("\n" + pathToFile);
		
	}
	
	protected void exitFromGui() {
		isRun = false;
		try {
			serverPortSender.close();
			serverPortListner.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	private void initSock() {
		try {
        	inF = serverSocket.getInputStream();
        	//dinF = new DataInputStream(inF);

        	outF = serverSocket.getOutputStream();
        	//doutF = new DataOutputStream(outF);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Socket was failed");
			e.printStackTrace();
		} 
	}
	
	private void initServerPortLS() {
		serverPortListner = new ServerPortListner(inF, this);
		serverPortSender = new ServerPortSender(outF, this);
		
		serverPortListner.start();
		serverPortSender.start();
	}
	

	
	//����������� �����������
	public void setInterrup() {
		Thread.currentThread().interrupt();
	}
	
	//�������� �����
	synchronized public void fileSend() {
		serverPortSender.pauseOn();
		File fts = new File(pathToFile+"/"+file);
		ExportFile _ef = new ExportFile(serverSocket,fts,this);
		
		System.out.println("�������� ������ _ef");
		_ef.start();
	}
	
	protected boolean isExistFile() {
		return new File(pathToFile+"/"+file).exists();
	}
	
	//����� �����
	synchronized public void fileRecive() {
		File fts = new File(pathToFile+"/"+file);
		ImportFile _if = new ImportFile(serverSocket,fts,this);
		System.out.println("�������� ������ _if");
		_if.start();
	}
}
