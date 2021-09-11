import java.net.*;
import java.net.Socket;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.Math;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashSet;

public class Cracker
{
	// Checks if a number is prime or not
	static boolean prime(long n)
	{
		if(n == 2) return true;
		if(n%2 == 0 || n==1) return false;
		for(long i=3; i<=Math.sqrt(n); i+=2)
		{
			if(n%i == 0) return false;
		}
		return true;
	}
	
	// Returns a string that has the prime factors of a number
	static String primeFact(long num) 
	{
		Map<Long, Integer> mp = new HashMap<Long, Integer>(); // Stores the highest power of each factor 
		LinkedHashSet<Long> hs = new LinkedHashSet<Long>(); // Stores the factors
		while(true)
		{
			if(prime(num))
			{
				hs.add(num);
				mp.put(num, 1);
				break;
			}
			if(num%2 == 0)
			{
				long two = 2;
				hs.add(two);
				mp.put(two, 1);
				num /= 2;
				while(num%2 == 0)
				{
					int pwr = mp.get(two);
					mp.put(two, pwr + 1);
					num /= 2;
				}
			}
			for(long i=3; i<=Math.sqrt(num); i+=2)
			{
				if(prime(i) && num%i == 0)
				{
					hs.add(i);
					mp.put(i, 1);
					num /= i;
					while(num%i == 0)
					{
						int pwr = mp.get(i);
						mp.put(i, pwr + 1);
						num /= i;
					}
				}
			}
			if(num != 1)
			{
				hs.add(num);
				mp.put(num, 1);
				break;
			}
			if(num == 1) break;
		}
		Iterator<Long> it = hs.iterator();
		String factors="";
		while(it.hasNext())
		{
			long res = it.next();
			if(mp.get(res) == 1) factors += Long.toString(res);
			else 
			{
				factors += Long.toString(res);
				factors += "^";
				factors += Long.toString(mp.get(res));
			}
			if(it.hasNext())
			{
				factors += ", ";
			}
		}
		return factors;
	}

	public static void main(String args[]) 
	{
		try
		{
			// Connect to the server through specified port
			InetAddress ip = InetAddress.getByName("localhost");
			Socket crackerSocket = new Socket(ip,5005);
			System.out.println("Connecting to the server....");
			
			// I/O Objects
			DataInputStream input = new DataInputStream(crackerSocket.getInputStream());
			DataOutputStream output = new DataOutputStream(crackerSocket.getOutputStream());
			Scanner scanner = new Scanner(System.in);
			
			// Enter password
			String passRequest = input.readUTF();
			System.out.println(passRequest);
			String pass = scanner.nextLine();
			output.writeUTF(pass);
			
			// Check if the server accepted the password or closed the connection
			String reply = input.readUTF();
			System.out.println(reply);
			if(reply.equals("Connected."))
			{
				while(true)
				{
					try
					{
						// Take, and display, request and associated client ID from server
						String request = input.readUTF();
						String id = input.readUTF();
						System.out.println("[" + id + "] wants " + request);
						
						// Parse request to Long and perform factorization 
						long number = Long.parseLong(request);
						String response = primeFact(number);
						
						// Return the result and associated client ID to server
						output.writeUTF(response);
						output.writeUTF(id);
						System.out.println("Answer sent to server.");
					}
					
					// Catch invalid requests (not a valid number)
					catch(NumberFormatException nfe)
					{
						break;
					}
				}
			}
			
			// Close objects
			crackerSocket.close();
			input.close();
			output.close();
			scanner.close();
		}			
		catch(IOException e)
		{
			System.out.println("Connection with the server is terminated.");
		}
	}	
 }