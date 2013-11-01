package ui;

import applicationLogic.Client;

/**
 * @author Allen
 * 
 */
public class Application {

	/**
	 * @param main function
	 */
	public static void main(String[] args) {
		Client c = new Client();
		c.run();
	}
}