package com.android.syncorganiser.server;

import java.io.IOException;
import java.io.OutputStream;

//Прослушиваем сокет и отправляем сообщения
public class ServerPortSender extends Thread {
	
	private OutputStream doutF;
	private Server _sv;
	
	public ServerPortSender(OutputStream dos, Server sv) {
		_sv = sv;
		doutF = dos;
	}
	
	public void run() {
		System.out.println("Вошёл в Runnalbe sendSocketMethod");

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
			System.out.println("Посылаем сообщение \"recive\"");
			//Хотим принять фаил
			doutF.write("recive".getBytes());
			_sv.fileRecive();
		} catch(Exception e) {System.out.println(""+e);}
	}
	
	public void Send(){
		try {
			System.out.println("Посылаем сообщение \"send\"");
			//Хотим отправить фаил
			doutF.write("send".getBytes()); 
			_sv.fileSend();
		} catch(Exception e) {System.out.println(""+e);}
	}

	public void pauseOn() {
		try {
			System.out.println("Посылаем сообщение \"pause\"");
			//Хотим отправить фаил
			doutF.write("pause".getBytes());
	} catch(Exception e) {System.out.println(""+e);}
		
	}
	
	public void pauseOff() {
		try {
			System.out.println("Посылаем сообщение \"resume\"");
			//Хотим отправить фаил
			doutF.write("resume".getBytes()); 
	} catch(Exception e) {System.out.println(""+e);}
		
	}
}
