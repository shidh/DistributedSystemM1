package communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Allen
 * 
 */
public class ClientSocket {

	public Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private Logger log;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public ClientSocket() throws IOException {
		log = Logger.getLogger(ClientSocket.class);
		PropertyConfigurator.configure("logs/log4j.properties");
	}

	/**
	 * Connect to a server socket
	 * 
	 * @param ip
	 * @param port
	 * @throws Exception
	 * @throws Exception
	 */
	public void connect(String ip, int port) throws NoRouteToHostException,
			ConnectException, SocketTimeoutException, UnknownHostException,
			IOException {
		try {
			socket = new Socket();
			SocketAddress endpoint = new InetSocketAddress(
					InetAddress.getByName(ip), port);
			socket.connect(endpoint, 2500);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		} catch (NoRouteToHostException e) {
			log.error("No network connection");
			throw e;
		} catch (ConnectException e) {
			log.error("Couldn't connect to server");
			throw e;
		} catch (SocketTimeoutException e) {
			log.error("Time out for 3 seconds, please check IP&Port");
			throw e;
		} catch (UnknownHostException e) {
			log.error("The host is wrong");
			throw e;
		} catch (IOException e) {
			log.error("No connection");
			throw e;
		} catch (Exception e) {
			log.error("There may be some unkown communication errors");
			e.printStackTrace();
		}
	}

	/**
	 * Send message to the server
	 * 
	 * @param m
	 *            the message to send
	 * @throws SocketTimeoutException
	 */
	public void send(byte[] m) throws Exception {
		try {
			outputStream.write(m);
			outputStream.flush();
		} catch (SocketException e) {
			log.error("There may be some unkown communication errors");
			throw e;
		} catch (IOException e) {
			log.error("There may be some communication errors");
			throw e;
		} catch (Exception e) {
			log.error("There may be some unkown communication errors");
			e.printStackTrace();
		}
	}

	/**
	 * Receive the message from server
	 * 
	 * @return the ASCII code which is in the byte array
	 * @throws SocketTimeoutException
	 * @throws SocketException
	 */
	public byte[] receive() throws Exception {
		byte[] maxresult = new byte[128000];
		try {
			int length = inputStream.read(maxresult);
			byte[] result = new byte[length];
			for (int i = 0; i < length; i++) {
				result[i] = maxresult[i];
			}
			return result;
		} catch (SocketException e) {
			log.error("There may be some unkown communication errors");
			throw e;
		} catch (IOException e) {
			log.error("There may be some communication errors");
			throw e;
		} catch (Exception e) {
			log.error("There may be some unkown communication errors");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Disconnect the socket connection
	 * 
	 * @return the boolean value whether is disConnected or not
	 */
	public boolean disConnect() throws Exception {
		try {
			if (!socket.isClosed()) {
				inputStream.close();
				outputStream.close();
				socket.close();
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException e) {
			log.error("There may be some unkown communication errors");
			throw e;
		} catch (SocketException e) {
			log.error("There may be some unkown communication errors");
			throw e;
		} catch (IOException e) {
			log.error("There may be some communication errors");
			throw e;
		} catch (Exception e) {
			log.error("There may be some unkown communication errors");
			e.printStackTrace();
		}
		return false;
	}
}