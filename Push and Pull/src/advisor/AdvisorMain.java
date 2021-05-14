/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab3
 */

package advisor;

import javax.swing.JFrame;


//advisor process 
public class AdvisorMain {

	public static void main(String[] args) throws Exception{
		//calling instance of the advisor interface
		AdvisorGUI advisor = new AdvisorGUI();
		//Enabling the close button feature in the advisor user interface
		advisor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//starting advisor operations
		advisor.startClient();
		
		
		
	}
	
}
