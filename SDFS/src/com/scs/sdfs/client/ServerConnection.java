package com.scs.sdfs.client;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.Gson;
import com.scs.sdfs.Constants;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.client.ConsoleListener.Methods;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CmdPutFileResponse;
import com.scs.sdfs.rspns.CommandResponse;

public class ServerConnection {

	private SSLContext sslContext;
	private SSLSocket socket;
	private Gson gson = new Gson();
	
	public ServerConnection(SSLContext sslContext) {
		this.sslContext = sslContext;
		init();
	}
	
	/**
	 * create the socket connection to the server
	 */
	private void init(){
		SSLSocketFactory factory = (SSLSocketFactory) sslContext.getSocketFactory();
		try {
			socket = (SSLSocket) factory.createSocket(Constants.LOCALHOST, Constants.SERVER_LISTENER_PORT);
			socket.setNeedClientAuth(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a CommandArgument object to the server
	 * @param argument
	 */
	public void sendCommand(CommandArgument argument){
		byte[] toSend = argument.toBytes();
		DataOutputStream dos = null;
		
		try {
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			dos.write(toSend);
			dos.flush();
		} catch (IOException e) {
			
		}
		
		if(dos != null){
			try {
				dos.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * reads response from server
	 * @return CommandResponse
	 */
	public CommandResponse readFromServer(Methods method){
		byte[] data = new byte[0];
		byte[] buffer = new byte[4096];
		int byteCount = -1;
		try {
			while ((byteCount = socket.getInputStream().read(buffer, 0,
					buffer.length)) > -1) {
				byte[] tempBuffer = new byte[data.length + byteCount];
				System.arraycopy(data, 0, tempBuffer, 0, data.length);
				System.arraycopy(buffer, 0, tempBuffer, data.length,
						byteCount);
				data = tempBuffer;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String _data = new String(data);
		switch(method){
		case GET:
			return gson.fromJson(_data, CmdGetFileResponse.class);
		case PUT:
			return gson.fromJson(_data, CmdPutFileResponse.class);
		default:
			return null;
		}
		
	}
	
	/**
	 * close the server connection
	 */
	public void close() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}