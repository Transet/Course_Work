package com.android.syncorganiser.server;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class CMain {
	
    public static void main(String[] args)  {
    	CMain main = new CMain(); //О да, обожаю свой код
    	main.newConn();
    }
    
    @SuppressWarnings("unused")
	private static void FatalError() {
		JFrame DFrame = new JFrame();
		JOptionPane.showMessageDialog(DFrame,"Произошла непоправимая ошибка.\nСообщите мне на почту: \"Tsikunov	.54@mail.ru\"\n мы разберемся.","Fatal error",JOptionPane.ERROR_MESSAGE);
		while(DFrame.isVisible()){}
		System.exit(-1);
    }
    
    private void newConn() {     //Открывает сервер который ждет конектора   
    	try {
    		new MultiServer().start();
    	} catch (Exception e) {
    		
    	}
	}

}
