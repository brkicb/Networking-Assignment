import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Objects;

/* EECS 3214 Assignment 2
 *
 * Code structure taken from http://www.di.ase.md/~aursu/ClientServerThreads.html
 * --- unecessary functionalities were cut from the original code, and needed functionalities were added ---
*/
public class Server
{
  	private static ServerSocket serverSocket = null;
  	private static Socket clientSocket = null;
  	private static final int maxClientsCount = 10;
  	private static final clientThread[] threads = new clientThread[maxClientsCount];

  	public static void main(String args[]) 
	{
    	// Port number
    	int portNumber = 28806;
 	
     	// Open a server socket on portNumber 28806   	
    	try 
		{
      		serverSocket = new ServerSocket(portNumber);
    	} 
		catch (IOException e) 
		{
      		System.out.println(e);
    	}
    	
    	// Create a client socket for each connection and pass it to a new client
     	// thread  	
    	while (true) 
		{
      		try 
			{
        		clientSocket = serverSocket.accept();
        		int i = 0;
        		for (i = 0; i < maxClientsCount; i++) 
				{
          			if (threads[i] == null) 
					{
            			(threads[i] = new clientThread(clientSocket, threads)).start();
            			break;
          			}
        		}
        		if (i == maxClientsCount) 
				{
          			PrintStream os = new PrintStream(clientSocket.getOutputStream());
          			os.println("Server too busy. Try later.");
          			os.close();
          			clientSocket.close();
        		}
      		} 	
			catch (IOException e) 
			{
       	 		System.out.println(e);
      		}
    	}
  	}
}

 // This is what allows multiple clients to join the server
class clientThread extends Thread 
{
	private String name = "";
  	private String clientName = null;
  	private DataInputStream is = null;
  	private PrintStream os = null;
  	private Socket clientSocket = null;
  	private final clientThread[] threads;
  	private int maxClientsCount;

  	public clientThread(Socket clientSocket, clientThread[] threads) 
	{
    	this.clientSocket = clientSocket;
    	this.threads = threads;
    	maxClientsCount = threads.length;
  	}

  	public void run() 
	{
    	int maxClientsCount = this.maxClientsCount;
    	clientThread[] threads = this.threads;

    	try 
		{
      		/*
       		* Create input and output streams for this client.
       		*/
      		is = new DataInputStream(clientSocket.getInputStream());
      		os = new PrintStream(clientSocket.getOutputStream());

      		// Message to show Client the various commands (JOIN, LEAVE, LIST)
      		os.println("\nTo join chat in the server type 'JOIN'\nTo leave the server type 'LEAVE'\nTo view a list of who is currently in the server type 'LIST'\n");
      		
      		while (true) 
			{
				String line = is.readLine();
				// When JOIN command is executed, have the Client enter their name and join the server
				if (line.trim().startsWith("JOIN"))
				{
					while (true) 
					{
        				os.println("Enter your name.");
        				name = is.readLine().trim();
        				if (name.indexOf('@') == -1) 		
        					break;        		
						else
          					os.println("Name shouldn't contain '@' symbol");		
      				}
					synchronized (this) 
					{
        				for (int i = 0; i < maxClientsCount; i++) 
						{
          					if (threads[i] != null && threads[i] == this) 
							{
            					clientName = "@" + name;
            					break;
          					}
        				}
      				}
				}
        		
				// When LEAVE command is executed, exit the loop 
        		else if (line.startsWith("LEAVE")) 
          			break;

				else if (line.startsWith("LIST")) 
				{
					os.println("Clients on the Server:");

					// Synchronize threads				
					synchronized (this)
					{
						// prints the names of the Clients
						for (int i = 0; i < maxClientsCount; i++) 
							if (threads[i] != null) 
								os.println(threads[i].name + " " + clientSocket.getLocalAddress() + ": " + clientSocket.getLocalPort());
					}
				}
				if (line.startsWith("@")) 
				{
         			String[] message = line.split("\\s", 2);
          			if (message.length > 1 && message[1] != null) 
					{
           				message[1] = message[1].trim();
           				if (!message[1].isEmpty()) 
						{
              				synchronized (this) 
							{
                				for (int i = 0; i < maxClientsCount; i++) 
								{
                  					if (threads[i] != null && threads[i] != this && threads[i].clientName != null && threads[i].clientName.equals(message[0])) 
									{
                    					threads[i].os.println("private message from: " + name + " >> " + message[1]);
                    					this.os.println("private message to: " + message[0].replaceAll("@", "") + " >> " + message[1]);
                   						break;
                  					}
                				}
              				}
            			}
          			}
        		} 
				else 
				{
          			synchronized (this) 
					{
            			for (int i = 0; i < maxClientsCount; i++)	
              				if (threads[i] != null && threads[i].clientName != null) 
								if ((!(Objects.equals(line, "JOIN"))) && (!(Objects.equals(line, "LEAVE"))) && (!(Objects.equals(line, "CONNECT"))) && (!(Objects.equals(line, "LIST"))))
                					threads[i].os.println("<" + name + "> " + line);
          			}
      			}
			}

      		os.println("*** You have left the chatroom ***");

      		
       		// Set the current thread variable to null so that a new client
       		// can be accepted by the server
       		
      		synchronized (this) 
			{
        		for (int i = 0; i < maxClientsCount; i++) 
          			if (threads[i] == this) 
            			threads[i] = null;  	
      		}
      		
       		// Close the input stream, Close the output stream and close the socket
      		is.close();
      		os.close();
      		clientSocket.close();
    	} 
		catch (IOException e) {}
	}
}
