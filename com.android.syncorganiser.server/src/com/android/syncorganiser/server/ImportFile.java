package com.android.syncorganiser.server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

public class ImportFile extends Thread {
	
	
	private Socket _curSock;
	private File _curFile;
	private Server _sv;
	public ImportFile(Socket curSock,File curFile,Server sv) {
		_curFile = curFile;
		_curSock = curSock;
		_sv = sv;
	}
	
	public void run() {
        try {
                DataInputStream din = new DataInputStream(_curSock.getInputStream());
                _sv.gui.setText("---------------------------------\n");
                _sv.gui.appendText("Прием нового файла: \n");
                long fileSize = din.readLong(); // получаем размер файла
                String fileName = din.readUTF(); //прием имени файла
                if(fileSize == 0 && fileName == "Error") {
                	_sv.gui.appendText("Произошла ошибка на стороне телефона.\n");
                    _sv.serverPortListner.unpause();
                    return;
                }
                _sv.gui.appendText("Имя файла: " + fileName+"\n");
                _sv.gui.appendText("Размер файла: " + fileSize + " байт\n");
                
                byte[] buffer = new byte[64*1024];
                FileOutputStream outF = new FileOutputStream(_curFile);
                int count,total = 0;
                    
                while (((count = din.read(buffer)) != -1) && (total <= fileSize)){               
                    outF.write(buffer, 0, count);
                    total+=count;
                }
                outF.flush();
                outF.close();
                _sv.gui.appendText("Файл принят\n---------------------------------\n");
                _sv.serverPortListner.unpause();
            
        }
	        catch(Exception e){
	            e.printStackTrace();
	            _sv.serverPortListner.unpause();
	        }
	}

}
