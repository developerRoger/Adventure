package com.firstTry.Adventure;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdventureTest {

	
	  public static void main(String[] args) { System.out.println(new
	  BCryptPasswordEncoder().encode("123")); }


	private ServerSocket ss;

	private Socket socket;

	private BufferedReader in;

	private PrintWriter out;

	public AdventureTest()

	{
		try

		{

			ss = new ServerSocket(1709);

			while (true)

			{

				socket = ss.accept();
				System.err.println(socket.getPort()+"laole");

				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				BufferedInputStream bf =new BufferedInputStream(socket.getInputStream());
				
				DataInputStream dis = new DataInputStream(bf);
				
				byte[] bytes = new byte[1024];
			    while (dis.read() != -1) {
			    	
                }
				out = new PrintWriter(socket.getOutputStream(), true);

				String line = in.readLine();
				System.out.println(line);

				out.println("you input is :" + line);

				out.close();

				in.close();

				socket.close();

			}

		}

		catch (IOException e) {
		}
		try {
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
/*	public static void main(String[] args) {
		new AdventureTest(); 
	}*/
}
