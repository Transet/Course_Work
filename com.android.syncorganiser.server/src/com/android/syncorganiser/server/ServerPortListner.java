package com.android.syncorganiser.server;

import java.io.IOException;
import java.io.InputStream;

//Прослушиваем сокет и получаем сообщения
public class ServerPortListner extends Thread {
	
	private InputStream dinF;
	private Server _sv;
	private boolean isPause = false;
	
	public ServerPortListner(InputStream dis, Server sv) {
		dinF = dis;
		_sv = sv;
	}
	
	@Override
	public void run() {
		System.out.println("Вошёл в Runnalbe listenSocketMethod");
		while(!this.isInterrupted()) {
			
			while(isPause){/*Do nothing*/}
			
			String str = InMes();
			if(str != null) {
				System.out.println("Succes in get mes from InMes() :"+str);
				switch(str) {
				case "send":
					//Нам хотят отправить фаил
					Recive();
					break;
				case "recive":
					//От нас хотят принять фаил
					Send();
					break;
				case "pause":
					pause();
					break;
					
				}
			}
			Thread.yield();
		}
	}

	public void close() throws IOException {
		this.interrupt();
		dinF.close();
		dinF = null;
	}
	
	public void pause() {
		isPause = true;
	}
	
	public void unpause() {
		isPause = false;
	}
	
	public void Send() {
		_sv.fileSend();
	}
	
	public void Recive() {
		_sv.fileRecive();
	}
	
	//Прием сообщения
	private String InMes()
	{
		byte[] buffer = new byte[256];
		int bytes; // bytes returned from read()
		String str=null;
    	try {
    		//System.out.println("Try to catch String in fuction InMes();");
			bytes = dinF.read(buffer);
			str = new String(buffer,0,bytes);
		} catch (Exception e) {
			//System.out.println("Ошибка! "+e.getMessage());
			return null;
		}
    	return str;
	}
}
