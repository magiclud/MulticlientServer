import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * A chat server that delivers public and private messages.
 */
public class MultiServer {

	// The server socket.
	private static ServerSocket serverSocket = null;
	// The client socket.
	private static Socket clientSocket = null;

	// maksymalna liczba klientow
	private static final int maxClientsCount = 2;
	// tablica z watkami wszystkich klientow
	private static final ClientThread[] threads = new ClientThread[maxClientsCount];

	public static ServerTranslator translator;
	

	public MultiServer() {
		
		}

	public void wlaczSie() {

		// The default port number.
		int portNumber = 3333;
		/*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can not choose a port less than 1023 if we are not privileged users
		 * (root).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a client socket for each connection and pass it to a new
		 * client thread.
		 */
		while (true) {
			try {
				// akceptuje wstepnie wszystkich klientow
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						// wyszukuje pierwszego wolnego miejsca w tablicy i
						// tworze watek klienta
						(threads[i] = new ClientThread(clientSocket, threads,
								this)).start();
						break;
					}
				}
				// new table
				// jesli jednak nie znalazlem wolnego miejsca to wyczerpany
				// zostal limit liczby jednoczesnie
				// obslugiwanych klientow
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(
							clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}

}

/**
 * Klasa reprezentujaca watek klienta
 * 
 */
class ClientThread extends Thread {

	private DataInputStream is = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private final ClientThread[] threads;
	private int maxClientsCount;
	private String playerID;
	MultiServer server;

	public ClientThread(Socket clientSocket, ClientThread[] threads,
			MultiServer server) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
		this.server = server;
		// this.playerID;
	}

	@Override
	public void run() {

		try {
			/* Create input and output streams for this client. */
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			os.println("Enter your name.");
			// sczytuje odpowiedz od klienta
			String name = is.readLine().trim();
			this.playerID = name;
			os.println("Hello " + name
					+ " to our chat room.\nTo leave enter /quit in a new line");
			// rozsylam odpowiedz do wszystkich innych klientow
			powiadomWszystkichKlietow("*** A new user " + name
					+ " entered the chat room !!! ***");
			while (true) {
				String line = is.readLine();
				// dzialam az ktos nie poda /quit
				if (line.startsWith("/quit")) {
					break;
				}
				if (line.startsWith(playerID)) {
					
					String[] splitted = line.split(":");
					ServerTranslator.startMethod(playerID, splitted[1]);
				}

				if (CommunicationBox.orderForServer != null &&
						CommunicationBox.whichPlayerServer == playerID) {
					String polecenie = CommunicationBox.orderForServer;
					os.println(polecenie);
					
					CommunicationBox.orderForServer = null;
					CommunicationBox.whichPlayerServer=null;
					
				}
				// cokolwiek odczytam od klienta to rozsylam do innych
				// powiadomWszystkichKlietow("<" + name + "> " + line);
			}
			// jesli wyszedlem z petli to znaczy ze skonczylem, wysylam ta
			// inforamcje rowniez do wszystkich klientow
			powiadomWszystkichKlietow("*** The user " + name
					+ " is leaving the chat room !!! ***");

			os.println("*** Bye " + name + " ***");

			/*
			 * Clean up. Set the current thread variable to null so that a new
			 * client could be accepted by the server.
			 */
			posprzatajPolaczenie();

			/*
			 * Close the output stream, close the input stream, close the
			 * socket.
			 */
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}

	private void posprzatajPolaczenie() {
		synchronized (this) {
			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] == this) {
					threads[i] = null;
				}
			}
		}
	}

	private void powiadomWszystkichKlietow(String wiadomosc) {
		synchronized (this) {
			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] != null && threads[i] != this) {
					threads[i].os.println(wiadomosc);
				}
			}
		}
	}
}