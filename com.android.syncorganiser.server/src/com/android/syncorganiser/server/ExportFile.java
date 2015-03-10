package com.android.syncorganiser.server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class ExportFile extends Thread {
	
	private Socket _curSock;
	private File _curFile;
	protected Server _sv;
	public ExportFile(Socket curSock,File curFile, Server sv) {
		_curFile = curFile;
		_curSock = curSock;
		_sv = sv;
	}
	
	public void run()
	{
        DataOutputStream outD; 
        try{

        	Thread.sleep(1000);
            outD = new DataOutputStream(_curSock.getOutputStream());          
	    		if(!_sv.isExistFile()) {
	    			_sv.gui.setText("Нет бекапа для востановления");
	                outD.writeLong(0);
	                outD.writeUTF("fileIsNotExist");
	                outD.flush();
	                return;
	    		}
	    		else {
	                outD.writeLong(_curFile.length());
	                outD.writeUTF(_curFile.getName());
	            	_sv.gui.setText("---------------------------------\nПередается файл\n");
	            	_sv.gui.appendText("Передача нового файла: \n");  
	                _sv.gui.appendText("Имя файла: " + _curFile+"\n");
	                _sv.gui.appendText("Размер файла: " + _curFile.length() + " байт\n");
	                FileInputStream in = new FileInputStream(_curFile);
	                byte [] buffer = new byte[64*1024];
	                int count;
	                while((count = in.read(buffer)) != -1){
	                    outD.write(buffer, 0, count);
	                }
	                
	                _sv.gui.appendText("Закончил передачу!!!");
	                outD.flush();
	                in.close(); 
	    		}
        }
        catch(IOException e){
            e.printStackTrace();
        } catch(InterruptedException e) {
        	e.printStackTrace();
        }
        finally {
        	;//_sv.serverPortSender.pauseOff();
        }
	}
   
}
