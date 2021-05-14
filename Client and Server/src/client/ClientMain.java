/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab1
 */

package client;
//for ui of the client
import javax.swing.*;
//class to initiate client 
public class ClientMain {

	public static void main(String[] args) throws Exception{
		//calling instance of the client interface
		ClientGUI client = new ClientGUI();
		//Enabling the close button feature in the client gui
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//starting client operations
		client.startClient();
	}
}
