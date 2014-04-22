import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


public class MultiClient implements Runnable {

	private static Socket clientSocket = null;
	private static PrintStream os = null;	//outout stream
	private static DataInputStream is = null; //input stream
	
	private static BufferedReader inputLine = null; 
	private static boolean closed = false;
	
	public static void mian(String[] args){
		//default port 
		int portNumber = 7777;
		//default host 
		String host = "localhost";
		
		if(args.length < 2){
			System.out.println("Usage: host: "+ host+ ", portNumber="+ portNumber);
		}
		else{
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}
		
		//open a socket on a given host and port, open input and output streams
		try{
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new PrintStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
		////}catch(UnknowHostException e){
		//	System.err.println("Don't know about host "+ host);
		}catch(IOException e){
			System.err.println("Couldn't get I/O for the connection to the host " + host);
		}
		//open a connection to on the portNumber and write some data
		if(clientSocket != null && os != null && is != null){
			try {
				//create thread to read wrom the server
				new Thread(new MultiClient()).start();
				while(!closed){
					os.println(inputLine.readLine().trim());
				}
				//close the outputStream, close the input stream, close the socket
				os.close();
				is.close();
				clientSocket.close();
			}catch(IOException e){
				System.err.println("IOException: " + e);
			}
		}
	}
	//create a thread to read from the server
	@Override
	public void run() {
		
		String responseLne;
		try{
			while((responseLne = is.readLine()) != null){
				System.out.println(responseLne);
				if(responseLne.indexOf("*** Cliet leave the connection") != -1){
					break;
				}
				if(responseLne.startsWith("from")){
					System.out.println("dziala");
				}
			}
			closed = true;
		}
catch(IOException e){
	System.err.println("IOException: " + e);
}		
	}

}
