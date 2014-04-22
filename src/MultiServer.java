import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;


public class MultiServer {
	
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	private int portNumber = 7777;	//default port number
	
	private static int maxClientsCount = 3;
	
	//array with threads of each client
	private static ClientThread[] threads = new ClientThread[maxClientsCount];
	
	public MultiServer(){
		//open server socket on portNumber 
				try{
					serverSocket = new ServerSocket(portNumber);
				}catch(IOException e){
					System.out.println(e);
				}
	}
	public static void main(String[] args){
		MultiServer turnOn = new MultiServer();
		turnOn.connect();
	}
	public void connect(){
		
		//create a client socket for each connection and pass it to a new clinet thread
		while (true){
			try{
				//accept each client
				clientSocket = serverSocket.accept();
				int i=0;
				for (i =0; i< maxClientsCount; i++){
					if(threads[i] == null){
						//find free place in array and create client's thread
						(threads[i] = new ClientThread(clientSocket, threads, this)).start();
					}
				}
				//if there are no free place in array, so there are enought clients 
				if(i == maxClientsCount){
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			}catch(IOException e){
				System.out.println(e);
			}
		}
	}
}

//class for client's thread
class ClientThread extends Thread implements Runnable{
	
	private Socket clientSocket = null;
	private ClientThread[] threads;
	private int maxClientCount;
	MultiServer server;
	
	private DataInputStream is = null;
	private PrintStream os = null;
	
	private String nameFile;
	private String path;
	
	public ClientThread(Socket clientSocket, ClientThread[] threads, MultiServer server){
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientCount = threads.length;
		this.server = server;
	}
	
	
	public void run(){
		try{
			//create input and output streams for this clinet
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			os.println("Tutaj wyswietle Ci informacje o plkikach do pobrania, podaj nazwe pliku i sciezke");
			
			//read answer from client
			 String file = is.readLine().trim();
			 this.nameFile = file;
			 String where = is.readLine().trim();
			 this.path = where;
			 
			 //server answer
			 os.println("You've choosen "+ nameFile + " and place for file " + path);
			 os.println("If you whant end connection enter /guit in a new line");
			 
			 while(true){
				 String line = is.readLine();
				 
				 //i work until sb write /guit
				 
				 if(line.startsWith("/guit")){
					 break;
				 }
				// if(line.startsWith(prefix))
			 }
			 os.println("*** Cliet leave the connection");
			 
			 endThread();
			 
			 is.close();
			 os.close();
			 clientSocket.close();
		}catch(IOException e){
			
		}
		
	}


	private void endThread() {
		synchronized(this){
			for(int i =0; i< maxClientCount; i++){
				if(threads[i] == this){
					threads[i] = null;
				}
			}
		}
	}
}