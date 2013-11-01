package applicationLogic;

import communication.ClientSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Allen
 * 
 */
public class Client {

	private BufferedReader br;
	private ClientSocket cs;
	private String message = "";
	private String ori_command = "";
	private int command = 0;
	private int port = 0;
	private String ipAddress = null;
	private boolean isPortAvailable = false;
	private boolean isConnected = false;
	private Logger log;
	private final int CONNECT_COMMAND = 1;
	private final int SEND_COMMAND = 2;
	private final int DISCONNECT_COMMAND = 3;
	private final int QUIT_COMMAND = 4;
	private final int ERROR_COMMAND = 5;
	private final int HELP_COMMAND = 6;
	private final int RETURN_COMMAND = 7;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 */
	public Client() {
		try {
			cs = new ClientSocket();
		} catch (IOException e) {
			System.out.println("Can NOT find the log4j configureation file");
		}

		log = Logger.getLogger(ClientSocket.class);
		PropertyConfigurator.configure("logs/log4j.properties");
	}

	/**
	 * Start the human interface
	 * 
	 * @throws Exception
	 */
	public void run() {
		System.out
				.println("EchoClient> "
						+ "Welcome to Echo Client, please type in command or type help!");

		while (true) {
			System.out.print("EchoClient> ");
			command = readCommand();
			switch (command) {
			case 1:
				try {
					if (!isConnected) {
						if (port > 65535) {
							System.out
									.println("EchoClient> port is too big to be out of range-65535:"
											+ port);
							break;
						}
						cs.connect(ipAddress, port);
						isConnected = true;
						byte[] bt = cs.receive();
						String str = new String(bt);
						System.out.print("EchoClient> " + str);

						// start monitor thread
						SocketMonitor monitor = new SocketMonitor();
						Thread t = new Thread(monitor);
						t.start();
					} else {
						System.out
								.println("EchoClient> You have already connected, please disconnect first");
					}

				} catch (NoRouteToHostException e) {
					System.out
							.println("No network connection, please check your network");
					break;
				} catch (ConnectException e) {
					System.out.println("Couldn't connect to server");
					break;
				} catch (SocketException e) {
					System.out
							.println("There may be some unkown communication errors, please check your network");
					break;
				} catch (SocketTimeoutException e) {
					System.out
							.println("Time out for 5 seconds, please check your the address&port");
					break;
				} catch (UnknownHostException e) {
					System.out
							.println("There seems like some problem for the host, "
									+ "please try another ip addrwss");
					break;
				} catch (IOException e) {
					System.out
							.println("There may be some communication errors");
					break;
				} catch (Exception e) {
					System.exit(-1);
					break;
				}
				log.info("Success to connect to the echo server!");
				break;
			case 2:
				if (isConnected) {
					if (!isPortAvailable) {
						System.out.println("EchoClient> No network");
						break;
					}
					try {
						byte[] m = message.getBytes();
						cs.send(m);
						cs.receive(); // get the test connect response from
										// monitor thread
						byte[] r = cs.receive(); // get the real echo message
						String temp = new String(r);
						System.out.print("EchoClient> " + temp);

					} catch (SocketException e) {
						System.out
								.println("There may be some unkown communication errors");
						break;
					} catch (IOException e) {
						System.out
								.println("There may be some communication errors");
						break;
					} catch (Exception e) {
						System.exit(-1);
						break;
					}
					log.info("Success to send message:" + message);
				} else {
					System.out
							.println("EchoClient> "
									+ "You havn't connected! Using command \"help\" to get help!");
					log.error("Send message failed! Have not connected to the echo server!");
				}
				break;
			case 3:
				try {
					if (!isPortAvailable) {
						System.out.println("EchoClient> No network");
						break;
					}
					if (isConnected) {
						cs.disConnect();
						isConnected = false;
						System.out.println("EchoClient> "
								+ "Connection terminated:"
								+ cs.socket.getInetAddress() + "/"
								+ cs.socket.getPort());
						log.info("Disconnect with echo server!");
					} else {
						System.out
								.println("EchoClient> "
										+ "You havn't connected! Using command \"help\" to get help!");
						log.warn("Disconnect failed! Have not connected to the echo server!");
					}
				} catch (NullPointerException e) {
					System.out
							.println("Please connect firstly, Use command \"help\" to get help!");
					;
					break;
				} catch (Exception e) {
					System.exit(-1);
					break;
				}
				break;
			case 4:
				if (!isPortAvailable) {
					System.out.println("No network");
					break;
				}
				log.info("Quit Application!");
				System.out.println("EchoClient> " + "Application exit!");
				System.exit(-1);
				break;
			case 5:
				System.out.println("EchoClient> "
						+ "Error command! Using command \"help\" to get help!");
				log.warn("Error command!" + ori_command);
				break;
			case 6:
				System.out.println("EchoClient> "
						+ "Connect Command: connect 'ip' 'port'");
				System.out.println("            "
						+ "Send Command: send 'message'");
				System.out.println("            "
						+ "Disconnect Command: disconnect");
				System.out.println("            " + "Quit Command: quit");
				System.out
						.println("EchoClient> "
								+ "Log Command: logLevel <level:   ALL | DEBUG |INFO | WARN |ERROR | FATAL |OFF)");
				log.info("Get help!");
				break;
			case 7:
				System.out.println("EchoClient> ");
				break;
			default:
				log.warn("What happened?");
				break;

			}
		}
	}

	/**
	 * Parse the command from user
	 * 
	 * @return the command type representing by an integer
	 */
	private int readCommand() {
		br = new BufferedReader(new InputStreamReader(System.in));
		try {
			ori_command = br.readLine();
			String temp[] = ori_command.split(" ");
			if (temp[0].equalsIgnoreCase("connect")) {
				if (temp.length > 2) {
					ipAddress = temp[1].toString();
					port = Integer.valueOf(temp[2].toString());
					return CONNECT_COMMAND;
				} else {
					return ERROR_COMMAND;
				}
			} else if (temp[0].equalsIgnoreCase("send")) {
				if (temp.length > 1) {
					message = ori_command.substring(5) + "\r";
					return SEND_COMMAND;
				} else {
					return ERROR_COMMAND;
				}
			} else if (temp[0].equalsIgnoreCase("disconnect")) {
				return DISCONNECT_COMMAND;
			} else if (temp[0].equalsIgnoreCase("quit")) {
				return QUIT_COMMAND;
			} else if (temp[0].equalsIgnoreCase("help")) {
				return HELP_COMMAND;
			} else if (temp[0].isEmpty()) {
				return RETURN_COMMAND;
			} else {
				return ERROR_COMMAND;
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return ERROR_COMMAND;
		}
	}

	/**
	 * Thread class to listen the socket connection in realtime
	 * 
	 */
	class SocketMonitor extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					cs.connect(ipAddress, port);
					isPortAvailable = cs.socket.isConnected();
					try {
						sleep(500);
					} catch (InterruptedException e) {
						return;
					}
				} catch (NoRouteToHostException e) {
					log.error("No network connection");
					// cs.socket = null;
					System.out
							.println(" Attention, no network connection detected ");
					System.out.println("EchoClient> ");
					isPortAvailable = false;
					isConnected = false;
					return;

				} catch (IOException e) {
					log.error("Error network connection");
					// cs.socket = null;
					System.out.println(" Attention, network disconnected ");
					System.out.println("EchoClient> ");
					isPortAvailable = false;
					isConnected = false;
					return;
				}
			}
		}
	}
}
