package com.android.syncorganiser.server;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class CMain {
	
    public static void main(String[] args)  {
    	CMain main = new CMain(); //� ��, ������ ���� ���
    	main.newConn();
    }
    
    @SuppressWarnings("unused")
	private static void FatalError() {
		JFrame DFrame = new JFrame();
		JOptionPane.showMessageDialog(DFrame,"��������� ������������ ������.\n�������� ��� �� �����: \"Tsikunov	.54@mail.ru\"\n �� ����������.","Fatal error",JOptionPane.ERROR_MESSAGE);
		while(DFrame.isVisible()){}
		System.exit(-1);
    }
    
    private void newConn() {     //��������� ������ ������� ���� ���������   
    	try {
    		new MultiServer().start();
    	} catch (Exception e) {
    		
    	}
	}

}
