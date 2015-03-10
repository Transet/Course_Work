package com.android.syncorganiser.server;

import java.io.IOException;
import java.io.OutputStream;

//������������ ����� � ���������� ���������
public class ServerPortSender extends Thread {
	
	private OutputStream doutF;
	private Server _sv;
	
	public ServerPortSender(OutputStream dos, Server sv) {
		_sv = sv;
		doutF = dos;
	}
	
	public void run() {
		System.out.println("����� � Runnalbe sendSocketMethod");

		while(!Thread.interrupted()) {
			Thread.yield();
		}
	}
	
	public void close() throws IOException {
		this.interrupt();
		doutF.close();
		doutF = null;
	}
	
	public void Recive(){
		try {
			System.out.println("�������� ��������� \"recive\"");
			//����� ������� ����
			doutF.write("recive".getBytes());
			_sv.fileRecive();
		} catch(Exception e) {System.out.println(""+e);}
	}
	
	public void Send(){
		try {
			System.out.println("�������� ��������� \"send\"");
			//����� ��������� ����
			doutF.write("send".getBytes()); 
			_sv.fileSend();
		} catch(Exception e) {System.out.println(""+e);}
	}

	public void pauseOn() {
		try {
			System.out.println("�������� ��������� \"pause\"");
			//����� ��������� ����
			doutF.write("pause".getBytes());
	} catch(Exception e) {System.out.println(""+e);}
		
	}
	
	public void pauseOff() {
		try {
			System.out.println("�������� ��������� \"resume\"");
			//����� ��������� ����
			doutF.write("resume".getBytes()); 
	} catch(Exception e) {System.out.println(""+e);}
		
	}
}
