/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab3
 */

package student;
//for ui of the client
import javax.swing.*;
//class to initiate client 
public class StudentMain {

	public static void main(String[] args) throws Exception{
		//calling instance of the client interface
		StudentGUI client = new StudentGUI();
		//Enabling the close button feature in the client user interface
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//starting client operations
		client.startClient();
		
		
		
	}
}
