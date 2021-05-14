/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab3
 */
package notification;

import javax.swing.JFrame;


//notification process
public class NotificationMain {

	//intializing notification
	public static void main(String[] args) throws Exception{
		//making an object of notification to get gui
	NotificationGUI noti = new NotificationGUI();
	//making gui able to close by pressing the red cross button and exiting the process
	noti.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//starting the notification process
	noti.startClient();
}
}
