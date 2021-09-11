import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server
{
	// Declare a socket for cracker and another for client
	static ServerSocket serverSocket;
	static ServerSocket serverSocket2;
	
	// Client ID and map to associate clients with IDs
	static int ID = 1;
	static Map<Socket, Integer> ids = new HashMap<Socket, Integer>();
	
	// Password to be requested from crackers
	final static String PASSWORD = "bonus";
	
	// Request to be taken from client and response to be returned back 
	static String newRequest=""; 
	static String response="";
	
	public static void main(String arg[])
	{
		try
		{
			// Cracker port 5005
			serverSocket = new ServerSocket(5005);
			
			// Client port 6006
			serverSocket2 = new ServerSocket(6006);
			
			System.out.println("The server is booted up.");
			
			while(true)
			{
				// Accept cracker connection, declare I/O, and prompt for password 
				Socket crackerSocket = serverSocket.accept();
				DataOutputStream crackoutput = new DataOutputStream(crackerSocket.getOutputStream());
				DataInputStream crackinput = new DataInputStream(crackerSocket.getInputStream());
				System.out.println("A new cracker is trying to connect to the server...");
				crackoutput.writeUTF("Enter the password: ");
				String reply = crackinput.readUTF();
				
				// Check if password is correct and react accordingly
				if(reply.equals(PASSWORD))
				{
					System.out.println("A new cracker is connected.");
					crackoutput.writeUTF("Connected.");
				}
				else
				{
					System.out.println("Cracker entered a wrong password and was dismissed.");
					crackoutput.writeUTF("Wrong password, closing connection...");
					crackerSocket.close();
					continue;
				}
				
				// Accept client connection, associate ID with client, then increment ID
				Socket clientSocket = serverSocket2.accept();
				System.out.println("A new client [" + ID + "] is connected to the server");
				ids.put(clientSocket,ID);
				ID++;
				
				// Start new thread for client
				Thread client = new ClientConnection(clientSocket, crackoutput, crackinput);
				client.start();
			}
		}
		catch(Exception e)
		{ 
			System.out.println("Problem with the socket server.");
		}
	}
	
	static class ClientConnection extends Thread
	{
		// Member variables (I/O declared here is for cracker)
		final private Socket clientSocket;
		final public DataOutputStream crackOutput;
		final public DataInputStream crackInput;
		
		// Constructor
		public ClientConnection(Socket clientSocket, DataOutputStream crackOutput, DataInputStream crackInput) 
		{ 
			this.clientSocket = clientSocket;
			this.crackOutput = crackOutput;
			this.crackInput = crackInput;
		}
		
		@ Override
		public void run() 
		{
			String id = Integer.toString(ids.get(clientSocket));
			
			try
			{
				// Declare I/O for client thread and send welcome message
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				output.writeUTF("Connected to server. :) \nh or help for use guide.");
				
				while(true)
				{
					try
					{
						// Take, and display, request from the client. Then react accordingly
						newRequest = input.readUTF();
						System.out.println("[" + id + "] sent " + newRequest);
						
						if(newRequest.equals("h") || newRequest.equals("help"))
						{
							output.writeUTF("Integer prime factorization is supported, input a valid number (greater than one). \n'exit' to quit.");
							continue;
						}
						
						if(newRequest.equals("exit"))
						{
							System.out.println("Closed connection with client [" + id + "]");
							clientSocket.close();
							break;
						}
						
						// Test validity of client request
						long Test = Long.parseLong(newRequest);
						if(Test <= 1)
						{
							output.writeUTF("<invalid input> h or help for use guide.");
							continue;
						}
						
						// Send request, if valid, and client ID to cracker
						crackOutput.writeUTF(newRequest);
						crackOutput.writeUTF(id);
						
						// Take factorization result and associated ID from cracker
						response = crackInput.readUTF();
						id = crackInput.readUTF();
						
						// Send result to associated client
						output.writeUTF(response);
					}
					
					// Catch invalid input and display help command
					catch(NumberFormatException nfe)
					{
						output.writeUTF("<invalid input> h or help for use guide.");
						continue;
					}
				}
				
				// Close objects
				input.close();
				output.close();
			}	 
			
			catch(IOException e)
			{ 
				System.out.println("Connection with [" + id + "] is closed. Terminate the assigned cracker.");
			}
		}
	}
 }