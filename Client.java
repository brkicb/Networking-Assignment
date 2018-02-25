import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Objects;

 /* EECS 3214 Assignment 2
 *
 * Code structure taken from http://www.di.ase.md/~aursu/ClientServerThreads.html
 * --- unecessary functionalities were cut from the original code, and needed functionalities were added ---
 * Not a lot of code modification was needed to be made to the client, so it is very similar to the original
 */
public class Client implements Runnable 
{
    private static Socket clientSocket = null;
	private static Socket privateClientSocket = null;	
	
	private static ServerSocket privateServerSocket = null;
	private static Socket privateClientAcceptSocket = null;

    private static PrintStream os = null;
    private static DataInputStream is = null;

	private static PrintStream pos = null;
	private static DataInputStream pis = null;

    private static BufferedReader inputLine = null;
	private static BufferedReader privateInputLine = null;

    private static boolean closed = false;
	private static boolean privateClosed = false;
  
    public static void main(String[] args) 
	{
    	// The Port Number
    	int portNumber = 28806;
    	// The host
    	String host = "localhost";
    	
     	// Open a socket on a given host and port. Open input and output streams    
   		try 
		{
			clientSocket = new Socket(host, portNumber);	
        	inputLine = new BufferedReader(new InputStreamReader(System.in));
        	os = new PrintStream(clientSocket.getOutputStream());
        	is = new DataInputStream(clientSocket.getInputStream());
    	} 
		catch (UnknownHostException e) 
		{
       		System.err.println(host + " is unknown");
    	} 
		catch (IOException e) 
		{
    	    System.err.println("I/O not received from the connection to " + host);
    	}

    	// If the variables were initialized
    	if (clientSocket != null && os != null && is != null) 
		{
    	    try 
			{
        		// Create a thread to read from the server
        		new Thread(new Client()).start();
        		while (!closed)
          			os.println(inputLine.readLine().trim());
				        	       		
        	 	// Close the output stream, input stream and the client socket 	
        		os.close();
        		is.close();
       	    	clientSocket.close();
    		} 
			catch (IOException e) 
			{
				System.err.println("IOException:  " + e);
    		}
    	}
	}
   
    // Create thread to read from the server
    public void run() 
	{
    	String responseLine;
    	try 
		{
      		while ((responseLine = is.readLine()) != null) 
			{
        		System.out.println(responseLine);
        		if (responseLine.indexOf("*** ") != -1)
          			break;
      		}
      		closed = true;
    	} 
		catch (IOException e) 
		{
      		System.err.println("IOException:  " + e);
    	}
  	}
}
