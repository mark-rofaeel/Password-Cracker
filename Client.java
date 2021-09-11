import java.net.*;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Client
{
	public static void main(String args[]) 
	{
		try
		{
			// Connect to the server through specified port
			InetAddress ip = InetAddress.getByName("localhost");
			Socket clientSocket = new Socket(ip,6006);
			System.out.println("Connecting to the server....");
			
			// I/O Objects
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			Scanner scanner = new Scanner(System.in);
			
			// Confirm connection with the server
			String connectionconfirm = input.readUTF();
			System.out.println(connectionconfirm);
			
			while(true)
			{
				// Type number to factorize and send it to server
				String request = scanner.nextLine();
				output.writeUTF(request);
				
				// Print help menu taken from server if the help cmd was entered
				if(request.equals("h") || request.equals("help"))
				{
					String help = input.readUTF();
					System.out.println(help);
					continue;
				}
				
				// Close the connection if the exit cmd was entered
				if(request.equals("exit"))
				{
					System.out.println("Closing connection with server....");
					clientSocket.close();
					System.out.println("Connection is closed.");
					break;
				}
				
				// Take factorization result from server
				String response = input.readUTF();
				System.out.println("Prime Factors: " + response);
			}
			
			// Close objects
			scanner.close();
			input.close();
			output.close();
		}			
		catch(IOException e)
		{
			System.out.println("Connection with the server is terminated.");
		}
	}
}