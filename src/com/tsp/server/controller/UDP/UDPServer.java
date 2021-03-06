package com.tsp.server.controller.UDP;

import com.tsp.server.model.ServerModel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created with IntelliJ IDEA.
 * User: Tim
 * Date: 10/14/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class UDPServer extends Thread
{
	private static boolean quit =false;
	ServerModel model;

	public UDPServer(ServerModel serverModel)
	{
		super("UDP Server");
		this.model = serverModel;
	}

	@Override
	public void run()
	{
		DatagramSocket serverSocket = null;
		try
		{
			serverSocket = new DatagramSocket(12000);
			while(!quit)
			{
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				new Thread(new RespondWorker(serverSocket, receivePacket, model)).start();
				Thread.sleep(50);
			}
		}
		catch (SocketException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		catch (IOException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		finally
		{
			if(serverSocket != null && serverSocket.isBound())
				serverSocket.close();
		}
	}

	public static void quit()
	{
		quit = true;
	}

}
