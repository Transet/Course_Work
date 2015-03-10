package com.android.synchronizercontacts;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "Syncronizer Contacts"; //Log.d(TAG, "");
	
	private File fileipAdressCache = null ;
	private final String fileName = "ipAdressCache.txt";
	private final String fullPathContacts = "/data/data/com.android.providers.contacts/databases/";
	private String fullPathBackup;
	private final String contacts = "contacts.db";
	private final String contacts2 = "contacts2.db";
	private String[] ipAdressCache;
	
	private boolean tryToConnectToLastPC = false;
	private boolean isReciveSucsess = false;
	
	private Context context = null;
	
	Button btnConnect,btnSend,btnRecive,btnBackup;
	TextView textViewOnCenter;
	
	private Socket mSocket = null;
	private int socketPort = 7172;
	
	private ConnectedThread mConnectedThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 btnConnect = (Button)findViewById(R.id.btnconnect);
		 btnConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG,"btnConnect OnClick()...");
				whatIShouldChoose();
			}
		});
		 btnRecive = (Button)findViewById(R.id.btnRecive);
		 btnRecive.setVisibility(View.INVISIBLE);
		 btnRecive.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mConnectedThread.write("recive");
				mConnectedThread.reciveFile();

			}
		});
		 btnBackup = (Button)findViewById(R.id.btnBackup);
		 btnBackup.setVisibility(View.VISIBLE);
		 btnBackup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/* Действия для возвращения бекапа в родную систему */
				backup();
			}
		});
		 btnSend = (Button)findViewById(R.id.btnSend);
		 btnSend.setVisibility(View.INVISIBLE);
		 btnSend.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mConnectedThread.write("send");
					mConnectedThread.writeFile();
				}
			});
		 textViewOnCenter = (TextView)findViewById(R.id.textView1);
		 textViewOnCenter.setText("Для старта работы нажмите на кнопку \"Подключение\"");
		 textViewOnCenter.setMovementMethod(new ScrollingMovementMethod());
		 context = this;
	}
	
	private void makeBackupDir() {
		Process process;
		int ret;
		try {
			Log.d(TAG,"Попытка создать папку backup...");
			process = Runtime.getRuntime().exec("su");

			// Поток ввода
			DataOutputStream os = new DataOutputStream(process.getOutputStream());

			os.writeBytes("mkdir -p "+fullPathBackup);
			os.flush();
			os.close();

			ret = process.waitFor();
			if(ret != 0)
			{/*Произошла ошибка.*/
				Log.d(TAG,"Хьюстон, мы провалились. Папка не была создана");
			}
			Log.d(TAG,"Папка была успешно создана");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void addRoot(){
		Process process;
		int ret;
		try {
			Log.d(TAG,"Попытка получить рут права...");
			process = Runtime.getRuntime().exec("su");

			// Поток ввода
			DataOutputStream os = new DataOutputStream(process.getOutputStream());

			os.writeBytes("ls /data");
			os.flush();
			os.close();

			ret = process.waitFor();
			if(ret != 0)
			{/*Произошла ошибка.*/
				Log.d(TAG,"Хьюстон, мы провалились. Рут права не дали... Самоликвидируюсь...");
				System.exit(1);
			}
			Log.d(TAG,"рут права были успешно представлены");
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void makeInternalBackup() {
		makeBackupDir();
		Log.d(TAG,"Попытка сохранить файл для бекапа...");
		String existBackUp = whatIsExistInBackUp();
		if(existBackUp != null) {
			funcRmFile(fullPathBackup + existBackUp);
		}
		String contact = whatIsExist();
		Log.d(TAG,"функция whatIsExist() вернула: "+contact);
		if(contact == null) {
			Log.d(TAG, "Хьюстон, у нас проблемы. Нет контактов...");
			textViewOnCenter.setText("Не возможно сделать бекап. Файл контактов не был найден.");
			return;
		}
		if(1 == funcCopyFile(fullPathContacts + contact, fullPathBackup )) {
			Log.d(TAG,"Произошла ошибка при создании бекапа");
			textViewOnCenter.setText("Произошла ошибка при создании бекапа");
			return;
		}
		Log.d(TAG,"Бекап был успешно создан.");
		textViewOnCenter.setText("Бекап был успешно создан.");
	}
	
	private void backup() {
		AlertDialog.Builder ad;
		 String title = "Выбор есть всегда.";
	        String message = "Что вы хотите сделать с бекапом?";
	        String button1String = "Создать";
	        String button2String = "Вернуть";
	        
	        ad = new AlertDialog.Builder(context);
	        ad.setTitle(title);  // заголовок
	        ad.setMessage(message); // сообщение
	        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int arg1) {
	            	makeInternalBackup();
	            }
	        });
	        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int arg1) {
	            	makeExternalBackup();
	            }
	        });
	        ad.setCancelable(true);
	        ad.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	            	/*Do nothing*/
	            }
	        });
	        ad.show();
	}
	
	private void makeExternalBackup() {
		makeBackupDir();
		Log.d(TAG,"Попытка выернуть сохраненые настройки...");
		String exitstBackUp = whatIsExistInBackUp();
		if(exitstBackUp == null) {
			Log.d(TAG,"Бекап не был найден...");
			textViewOnCenter.setText("Невозможно востановить контакты.\nБекап не был найден.");
			return;
		}
		String contact = whatIsExist();
		if(contact == null) {
			Log.d(TAG, "Хьюстон, у нас проблемы. Нет контактов...");
		} else {
			funcRmFile(fullPathContacts + contact);
		}
		
		if(1 == funcCopyFile(fullPathBackup + exitstBackUp, fullPathContacts + contact)) {
			Log.d(TAG,"Произошла ошибка при востановлении бекапа");
			textViewOnCenter.setText("Произошла ошибка при востановлении бекапа");
			return;
		}
		Log.d(TAG,"Бекап был успешно востановлен.");
		textViewOnCenter.setText("Бекап был успешно востановлен.");
	}
	
	private void whatIShouldChoose() {
		 AlertDialog.Builder ad;
		 String title = "Выбор есть всегда";
	        String message = "Подключится к прошлому компьютеру?";
	        String button1String = "Да";
	        String button2String = "Нет";
	        
	        ad = new AlertDialog.Builder(context);
	        ad.setTitle(title);  // заголовок
	        ad.setMessage(message); // сообщение
	        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int arg1) {
	                tryToConnectToLastPC = true;
					Connection newSocketConnect = new Connection();
					newSocketConnect.execute(textViewOnCenter);
	            }
	        });
	        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int arg1) {
	            	tryToConnectToLastPC = false;
					Connection newSocketConnect = new Connection();
					newSocketConnect.execute(textViewOnCenter);
	            }
	        });
	        ad.setCancelable(true);
	        ad.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	            	tryToConnectToLastPC = false;
					Connection newSocketConnect = new Connection();
					newSocketConnect.execute(textViewOnCenter);
	            }
	        });
	        ad.show();
	}
	
	  @Override
	  public void onResume() {
	    super.onResume();
	    Log.d(TAG, "...onResume - попытка соединения...");
	    
	    addRoot();
	    
	    fullPathBackup = context.getFilesDir() + "backup/" ;
	    if(fileipAdressCache == null) {
	    	Log.d(TAG,"Вошёл в файл");
	    	ipAdressCache = new String[256];
	    	fileipAdressCache = new File(context.getFilesDir(), fileName);
	    	if(fileipAdressCache.exists()) {
	    		getFileIpCache();
	    		Log.d(TAG, "ip address is :"+ipAdressCache[0]);
	    	}
	    }
	    
	    if(mSocket == null ) //Если сокет null то должны создать его
	    {
		    btnConnect.setVisibility(View.VISIBLE);
		    btnSend.setVisibility(View.INVISIBLE);
		    btnRecive.setVisibility(View.INVISIBLE);
	    } else {
	    	if(!mSocket.isConnected()){
			    btnConnect.setVisibility(View.VISIBLE);
			    btnSend.setVisibility(View.INVISIBLE);
			    btnRecive.setVisibility(View.INVISIBLE);
	    	}
		    btnConnect.setVisibility(View.INVISIBLE);
		    btnSend.setVisibility(View.VISIBLE);
		    btnRecive.setVisibility(View.VISIBLE);
		    textViewOnCenter.setText("Для старта работы нажмите на кнопку \"Подключение\"");
	    }
	  }
	  
	  private void getFileIpCache() {
	    	//Log.d(TAG,"Место куда сохраняются файлы: "+context.getFilesDir().getAbsolutePath());
	    	//FileOutputStream outputStream;
	    	FileInputStream inputStream;
	    	try
	    	{
	    	   inputStream = openFileInput(fileName);
	    	   byte[] buffer = new byte[256]; 
	    	   int bytes = inputStream.read(buffer);
	    	   inputStream.close();
	    	   
	    	   String strtmp = new String(buffer,0,bytes);
	    	   ipAdressCache[0] = new String(strtmp);
	    	   //outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
	    	   //outputStream.write("ARP CACHE".getBytes());
	    	   //outputStream.close();
	    	}
	    	catch (Exception e)
	    	{
	    	   Log.d(TAG,"Ошибки при чтении файла :"+e);
	    	}
	  }
	  
	  private void setFileIpCache(String str) {
	    	//Log.d(TAG,"Место куда сохраняются файлы: "+context.getFilesDir().getAbsolutePath());
	    	FileOutputStream outputStream;
	    	try
	    	{
	    	   outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
	    	   outputStream.write(str.getBytes());
	    	   outputStream.close();
	    	}
	    	catch (Exception e)
	    	{
	    	   Log.d(TAG,"Ошибки при записи в фаил :" + e);
	    	}
	  }
	  
	  
		private void makeBackUpAndSetFile() {
			if(!isReciveSucsess)
				return;
			Log.d(TAG,"Создание бекапа и применение новых настроект контактов...");
			String contactsExist = whatIsExist();
			if(contactsExist == null) {
				/* Блок отработки если нет файла */
				return;
			}
			//Создание бекапа 
			makeInternalBackup();
			
			//Удаление файла из интернала
			funcRmFile(fullPathContacts + contactsExist);
			
			//Копирование полученого файла
			funcCopyFile(context.getFilesDir()+"/" +contactsExist, fullPathContacts + contactsExist);
			
			//Удаление полученого файла
			funcRmFile(context.getFilesDir()+"/" +contactsExist);
		}
		
		private String whatIsExistInBackUp() {
			try
	    	{
	    	   if(!(new File(fullPathBackup+contacts).exists()))
	    		   throw new FileNotFoundException();
	    	   return contacts;
	    	} catch(Exception e)
	    	{
	    		 Log.d(TAG,"Ошибки при чтении файла :"+contacts+" "+e);
		    	try
		    	{
		    	   if(!(new File(fullPathBackup+contacts2).exists()))
			    	   throw new FileNotFoundException();
		    	   return contacts2;
		    	}
		    	catch(Exception e2) {
			    	   Log.d(TAG,"Ошибки при чтении файла :"+contacts2+" "+e2);
			    	   return null;
		    	}
	    	}
		}
		
		private String whatIsExist() {
			

	    	try
	    	{
	    	   if(!(new File(fullPathContacts+contacts).exists()))
	    		   throw new FileNotFoundException();
	    	   return contacts;
	    	} catch(Exception e)
	    	{
	    		 Log.d(TAG,"Ошибки при чтении файла :"+fullPathContacts+contacts+" "+e);
		    	try
		    	{
		    	   if(!(new File(fullPathContacts+contacts2).exists()))
			    	   throw new FileNotFoundException();
		    	   return contacts2;
		    	}
		    	catch(Exception e2) {
			    	   Log.d(TAG,"Ошибки при чтении файла :"+fullPathContacts+contacts2+" "+e2);
			    	   return null;
		    	}
	    	}
		}
		
		
		private int funcRmFile(String from) {
			/*	Блок try-catch обеспечивающий доступ */
			try
			 {					
				Process process = Runtime.getRuntime().exec("su");
				int ret;
				// Поток ввода
				DataOutputStream os = new DataOutputStream(process.getOutputStream());

				os.writeBytes("busybox rm "+from);
				os.flush();
				os.close();

				ret = process.waitFor();
				if(ret != 0)
					throw new IOException("Ошибка удаления");
				Log.d(TAG,"Файл "+from+" был успешно удален");
				return 0;
			 }
			 catch (IOException | InterruptedException e)
			 {
				 Log.d(TAG,"удаление файла не удалось "+e);
				 return 1;
			 }
		}
		
		
		private int funcCopyFile(String from,String to) {
			/*	Блок try-catch обеспечивающий доступ */
			try
			 {					
				Process process = Runtime.getRuntime().exec("su");
				int ret;
				// Поток ввода
				DataOutputStream os = new DataOutputStream(process.getOutputStream());

				os.writeBytes("busybox cp "+from+" "+to);
				os.flush();
				os.close();

				ret = process.waitFor();
				if(ret != 0)
					throw new IOException("Ошибка копировании");
				Log.d(TAG,"Файл "+from+" был успешно скопирован в "+to);
				return 0;
			 }
			 catch (IOException | InterruptedException e)
			 {
				 Log.d(TAG,"Копирование файла не удалось "+e);
				 return 1;
			 }
		}	
	  
	  
	  private class Connection extends AsyncTask<Object, String, Void> {

		  private boolean isGoodConnection = false;
		  
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			textViewOnCenter.setText("Идёт попытка подключения...");
		}
		  
		 public void getConnectionSocket() {
			  	Log.d(TAG,"...getConnectionSocket - попытка соединения...");
			  	InetAddress addr = null;
			  	
			  	if(tryToConnectToLastPC) {
				      Log.d(TAG, "ip address is :"+ipAdressCache[0]);
				      try {
				    	  addr = InetAddress.getByName(ipAdressCache[0]);
				    	  
				      	} catch (UnknownHostException e1) {
				      		return;
				      	}
				      
				      try{
				    	  Log.d(TAG,"... Попытка создать сокет"+addr); 
				    	  mSocket = new Socket();
				    	  mSocket.setSoTimeout(10*1000);
				          
				    	  Log.d(TAG, "...Соединяемся...");
				          mSocket.connect(new InetSocketAddress(addr, socketPort), 10*1000);
				          
				    	  if(!mSocket.isConnected())
				    		  return;
				    	  Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
				    	  isGoodConnection = true;
				    	  return;
				      } catch(IOException e) {
				    	  try {
				    		  mSocket.close();
				    		  return;
				    	  } catch(IOException e2) {
				    		  errorExit("Fatal Error", "In getConnectionSocket() and failed to close socket." + e.getMessage() + ".");
				    		  return;
				    	  }
				      }
			  	}
			  	
			    String[] manyIp = null;
				manyIp = getIPFromArpCache();
				if(manyIp==null)
				{
					Log.d(TAG, "...getIpFromCache() вернула null...");
					textViewOnCenter.setText("Unable to find a server");
					return;
				}
				
				for(int i=1;manyIp[i]!=null ;i++)
				{
			      //Log.d(TAG, "ip address is :"+ipAdressCache[0]);
			      
			      try {
			    	  addr = InetAddress.getByName(manyIp[i]);
			    	  
			      	} catch (UnknownHostException e1) {
			      		continue;
			      	}
			      
			      try{
			    	  Log.d(TAG,"... Попытка создать сокет"+addr); 
			    	  mSocket = new Socket();
			    	  mSocket.setSoTimeout(10*1000);
			          
			    	  Log.d(TAG, "...Соединяемся...");
			          mSocket.connect(new InetSocketAddress(addr, socketPort), 10*1000);
			          
			    	  if(!mSocket.isConnected())
			    		  continue;
			    	  Log.d(TAG, "...Соединение установлено и готово к передачи данных...");
			    	  isGoodConnection = true;
			    	  
			    	  setFileIpCache(manyIp[i]);
			    	  break;
			      } catch(IOException e) {
			    	  try {
			    		  mSocket.close();
			    	  } catch(IOException e2) {
			    		  errorExit("Fatal Error", "In getConnectionSocket() and failed to close socket." + e.getMessage() + ".");
			    	  }
			      }

				}
				
			  return;
		  }

		public String[] getIPFromArpCache() {
			    BufferedReader br = null;
			    int countIp=0;
			    try {
			        br = new BufferedReader(new FileReader("/proc/net/arp"));
			        String line;
			    	String[] ips = new String [255];
					while ((line = br.readLine()) != null) {
			            String[] splitted = line.split(" +");//RegExpresion " " и любое кол-во пробелов
			            if (splitted != null && splitted.length >= 4 ) {
			                // Basic sanity check
			                ips[countIp] = splitted[0];
			                countIp++;
			                
			            }
			        }
			        return ips;
			    } catch (Exception e) {
			        e.printStackTrace();
			    } finally {
			        try {
			            br.close();
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
			    }
			    return null;
			}
			
		  
		@Override
		protected Void doInBackground(Object... params) {
			getConnectionSocket();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(isGoodConnection) {
			     btnConnect.setVisibility(View.INVISIBLE);
			     btnSend.setVisibility(View.VISIBLE);
			     btnRecive.setVisibility(View.VISIBLE);
			     mConnectedThread = new ConnectedThread(mSocket);
			     mConnectedThread.start();
				 textViewOnCenter.setText("Подключение успешно!");
			} else {
			     btnConnect.setVisibility(View.VISIBLE);
			     btnSend.setVisibility(View.INVISIBLE);
			     btnRecive.setVisibility(View.INVISIBLE);
				 textViewOnCenter.setText("Неудалось установить подключение...");
			}
		}
		
	  }
	  

	  @Override
	  public void onPause() {
	    super.onPause();
	  
	    Log.d(TAG, "...In onPause()...");
	   /*if(mSocket!=null) {
		    try     {
		      mSocket.close();
		      btnConnect.setVisibility(View.VISIBLE);
		      btnSend.setVisibility(View.INVISIBLE);
		      btnRecive.setVisibility(View.INVISIBLE);
		    } catch (IOException e2) {
		      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
		    }
	   }*/
	  }
	
	  private void errorExit(String title, String message){
		  	Log.d(TAG, title + " - " + message);
		    textViewOnCenter.setText(title + " - " + message);
		    finish();
		  }
	
	  private class ConnectedThread extends Thread {
	        private final Socket mmSocket;
	        private final InputStream mmInStream;
	        private final OutputStream mmOutStream;
	      
	        private boolean isPause = false;
	        
	        public ConnectedThread(Socket socket) {
	            mmSocket = socket;
	            InputStream tmpIn = null;
	            OutputStream tmpOut = null;
	      
	            // Get the input and output streams, using temp objects because
	            // member streams are final
	            try {
	                tmpIn = socket.getInputStream();
	                tmpOut = socket.getOutputStream();
	            } catch (IOException e) { }
	      
	            mmInStream = tmpIn;
	            mmOutStream = tmpOut;
	        }
	      
	        public void run() {
	            byte[] buffer = new byte[256];  // buffer store for the stream
	            int bytes; // bytes returned from read()
	 
	            // Keep listening to the InputStream until an exception occurs
	            while (true) {
                	if(isPause)
                		continue;
	                try {
	                    // Read from the InputStream
	                    bytes = mmInStream.read(buffer);        // Получаем кол-во байт и само собщение в байтовый массив "buffer"
	                    String mes = new String(buffer,0,bytes);
	                    Log.d(TAG,"Сообщение \""+mes+"\" пришло...");
	                    switch(mes){
	                    case "send":
	                    	reciveFile();
	                    	break;
	                    case "recive":
	                    	writeFile();
	                    	break;
	                    case "pause":
	                    	onPauseListenMethod();
	                    	break;
	                    case "resume":
	                    	onResumeListenMethod();
	                    	break;
	                    }
	                    } catch (IOException e) {
	                    //Log.d(TAG,"Ошибка в ConnectedThread.run() при прослушке buffer="+buffer);
	                    continue;
	                } catch (Exception e) {
	                	System.exit(-1);
	                }
	            }
	        }
	      
	        /* Call this from the main activity to send data to the remote device */
	        public void write(String message) {
	            Log.d(TAG, "...Данные для отправки: " + message + "...");
	            byte[] msgBuffer = message.getBytes();
	            try {
	                mmOutStream.write(msgBuffer);
	            } catch (IOException e) {
	                Log.d(TAG, "...Ошибка отправки данных: " + e.getMessage() + "...");     
	              }
	        }
	      
	        /* Call this from the main activity to shutdown the connection */
	        
	        @SuppressWarnings("unused")
			public void cancel() {
	            try {
	                mmSocket.close();
	            } catch (IOException e) { }
	        }
	   
			public void writeFile() {
				write("pause");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendFileAT sfat = new sendFileAT();
				sfat.execute();
			}
			
			public void onPauseListenMethod() {
				isPause = true;
			}
			
			public void onResumeListenMethod() {
				isPause = false;
			}
			
			public void reciveFile() {
				reciveFileAT rfat = new reciveFileAT();
				rfat.execute();
			}
	        
			private class reciveFileAT extends AsyncTask<Void, String, Void> {

				@Override
				protected Void doInBackground(Void... params) {
					
					 try {
			                DataInputStream din = new DataInputStream(mmInStream);
			                long fileSize = din.readLong(); // получаем размер файла
			                String fileName = din.readUTF(); //прием имени файла
			                
			                if(fileSize == 0 && fileName == "fileIsNotExist")
			                {
			                	Log.d(TAG,"Ошибка принятия файла, нет бекапа на PC...");
			                	din.close();
			                	publishProgress("Произошла ошибка принятия файла.\nНет Бекапа файла на PC","set");
			                }
			                else {
				                Log.d(TAG,"Вошёл в функцию принятия файла...");
				                publishProgress("------\n","set");
				                publishProgress("Приём файла: \n","append");
				                
	
				                
				                publishProgress("Имя файла: " + fileName+"\n","append");
				                publishProgress("Размер файла: " + fileSize + " байт\n","append");
				                
				                byte[] buffer = new byte[64*1024];
				                FileOutputStream outF = new FileOutputStream(context.getFilesDir()+"tmp.contact");//pathContacts;
				                int count, total = 0;   
				                while ((count = din.read(buffer)) != -1){               
				                    total += count;
				                    outF.write(buffer, 0, count);
				                    if(total >= fileSize){
				                        break;
				                    }
				                }
				                Log.d(TAG,"Приём файла прошёл успешно...");
				                din.close();
				                outF.flush();
				                outF.close();
				                isReciveSucsess = true;
				                publishProgress("Файл принят","append"); 
			                }
			        }
			        catch(Exception e){
						Log.d(TAG,"Получение файла было прервано..." + e);
		                isReciveSucsess = false;
						publishProgress("Файл не был принят","append");
					
			        }
					
					return null;
				}
				
				@Override
				protected void onProgressUpdate(String... s) {
					//super.onProgressUpdate(s);
					if(s[0].isEmpty()) {
						textViewOnCenter.setText("Empty OnProgressUpdate");
						return;
					}
					switch(s[1]) {
					case "append" : textViewOnCenter.append(s[0]); break;
					case "set" : textViewOnCenter.setText(s[0]); break;
					}
			    }
				
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					onResumeListenMethod();
					makeBackUpAndSetFile();
				}
			}



			
			private class sendFileAT extends AsyncTask<Void, String, Void> {

				
				
				@Override
				protected Void doInBackground(Void... params) {
					File currentFile;
					FileInputStream inputStream;
					
					

			    	try
			    	{
			    	   if(!(new File(fullPathContacts+contacts).exists()))
			    		   throw new FileNotFoundException();
			    	   if(funcCopyFile(fullPathContacts+contacts, context.getFilesDir()+"/" + contacts) == 1 )
			    		   throw new Exception("Невозможно было скопировать файл");
			    	   
			    	   currentFile = new File(context.getFilesDir() +"/" + contacts);
			    	   inputStream =  new FileInputStream (currentFile);//= openFileInput(fullPathContacts+contacts);
			    	   Log.d(TAG,"Был открыт файл :"+contacts);
			    	} catch(Exception e)
			    	{
			    		 Log.d(TAG,"Ошибки при чтении файла :"+contacts+" "+e);
				    	try
				    	{
				    	   if(!(new File(fullPathContacts+contacts2).exists()))
					    	   throw new FileNotFoundException();
					       if(funcCopyFile(fullPathContacts+contacts2, context.getFilesDir()+"/" + contacts2) == 1 )
					    	   throw new Exception("Невозможно было скопировать файл");
					       
					       currentFile = new File(context.getFilesDir()+"/" + contacts2);
				    	   inputStream =  new FileInputStream (currentFile);
				    	   Log.d(TAG,"Был открыт файл :"+contacts2);
				    	}
				    	catch(Exception e2) {
					    	   Log.d(TAG,"Ошибки при чтении файла :"+contacts2+" "+e2);
					    	   return null;
				    	}
			    	}
					try {
		                Log.d(TAG,"Вошёл в функцию отправки файла...");
		                DataOutputStream outD = new DataOutputStream(mmOutStream);
		                publishProgress("------\n","set");
		                publishProgress("Передача файла: \n","append");
		                outD.writeLong((long)123123); // Передаем размер файла //ЗАГЛУШКА
		                outD.writeUTF(currentFile.getName()); // Передаём имя файла
		                publishProgress("Имя файла: " + "контакты"+"\n","append");
		                byte[] buffer = new byte[64*1024];
		                int count;
		                while ((count = inputStream.read(buffer)) != -1){               
		                    outD.write(buffer, 0, count);
		                }
		                Log.d(TAG,"Передача файла прошла успешно...");
		                inputStream.close();
		                outD.flush();
		                outD.close();
		                publishProgress("Файл передан","append");
					} catch(IOException e) {
						Log.d(TAG,"Передача файла была прервана..." +e);
						publishProgress("Файл не был передан","append");
					}
					return null; 
				}
				
				@Override
				protected void onProgressUpdate(String... s) {
					//super.onProgressUpdate(s);
					if(s[0].isEmpty()) {
						textViewOnCenter.setText("Empty OnProgressUpdate");
						return;
					}
					switch(s[1]) {
					case "append" : textViewOnCenter.append(s[0]); break;
					case "set" : textViewOnCenter.setText(s[0]); break;
					}
			    }
				
			}
			
	    }
}
