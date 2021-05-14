/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab3
 */
package MessageQueue;

import javax.swing.*;


//MQS process to handle the push and pool mesages
public class MQSMain {
	
	public static void main(String[] args) throws Exception
	{
		//calling instance of the client interface
		MQSGUI client = new MQSGUI();
		//Enabling the close button feature in the client user interface
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//starting client operations
		client.startClient();
	}
}