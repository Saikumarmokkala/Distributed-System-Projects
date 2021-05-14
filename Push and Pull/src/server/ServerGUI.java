/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab3
 */

package server;

import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;



public class ServerGUI extends JFrame 
{
	//arraylist to store messages from students
	ArrayList<String> Advisor = new ArrayList<String>();
	//arraylist to store messages from advisor
	ArrayList<String> Notification = new ArrayList<String>();
	//arraylist to store the threads
	ArrayList<ClientThread> al;
	//where the msgs to server will be displayed
	private JTextArea chatWindow;
	//socket where the server will intiate
	private ServerSocket serverSocket;
	//socket from where server will talk with client
	private Socket clientConnection;

	//constructor
	public ServerGUI()
	{
		// name the gui
		super("Server");
		//lable on the gui 
		JLabel label = new JLabel();
		add(label,BorderLayout.NORTH);
		label.setText("DS Lab 3");
		//text area where msgs will be displayed
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		chatWindow.setEditable(false);
		//seting the size of gui
		setSize(400, 300); //Sets the window size
		//setVisible(true);
		//setLocation(800,0);
		//initating the server gui at the center
		setLocationRelativeTo(null);
		//initiating the array list
		al = new ArrayList<ClientThread>();
	}
//main method which is being called by serveMain
	public void startRunning()
	{
		try{
			//server socket initializing where server will be listening for the connections
			serverSocket = new ServerSocket(7689); 
			//displaying after serve is initiated 
			showMessage(" Waiting for someone to connect at 7689... \n");
			//infine loop to here the client incoming connections
			while(true)
			{
				try{
					//Trying to connect and have conversation
					waitForConnection();

				}catch(EOFException eofException){
					//if error showing error msg
					showMessage("\n Server ended the connection! ");
				} 
			}
		} 
		catch (IOException ioException)
		{
			ioException.printStackTrace();
		}
	}


	//wait for connection, then display connection information
	void waitForConnection() throws IOException
	{
		//Storing client socket after accepting its connection
		clientConnection = serverSocket.accept();
		//Sending the client socket into the thread and initializing a thread
		ClientThread ct = new ClientThread(clientConnection);
		//adding thread into array list
		al.add(ct);
		//starting the thread
		ct.start();
	}

	//displaying msgs on the server gui textarea
	void showMessage(final String text)
	{
		//thread which keeps on running to put the msg on the textarea
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						chatWindow.append("\n"+text);
					}
				}
				);
	}
//checking if MQ server is on or off
boolean checkMQS()
{
	boolean chk=false;
	if(al.size()>1)
	{
	for( int j =al.size();--j>=0;)
	{
		//getinng thread fromm arraylist
		ClientThread k = al.get(j);
		//matching there username
		if(k.username.equalsIgnoreCase("MQS"))
		{
			chk=true;
				
		}
		
		}
	}
	return chk;
}
//Sendinf messages via MQ server
void sendMQS(String msg) throws IOException
{

		
		for( int j =al.size();--j>=0;)
		{
			//getinng thread fromm arraylist
			ClientThread k = al.get(j);
			//matching there username
			if(k.username.equalsIgnoreCase("MQS"))
			{
				//sending msgs				
					k.output.writeObject(msg);
					k.output.flush();
					//showMessage("\n" + s[2]+ " (Personal Msg)");
				}
			}

}



//getting number of user present in the network at the particular instance
	synchronized String getUsers() throws IOException
	{
		//just some variable to recogines it at the client end
		String list="Killo";
		for( int l =al.size();--l>=0;)
		{
			//getting client thread from arraylist
			ClientThread p = al.get(l);
			//getting their user name
			list = list + "-"+p.username ;
		}
		return list;
	}
	



// class where client thread are handled 
	public class ClientThread extends Thread
	{
		//client socket instance
		Socket clientConnection;
		//object for sending msgs to client
		ObjectOutputStream output;
		//object for getting msgs from client
		ObjectInputStream input;
		//username of the client
		String username;
		//int varaiable to checking the intial connection and store the username 
		int count = 0;

		//constructor to get client socket
		public ClientThread(Socket clientConnection) 
		{
			this.clientConnection = clientConnection;
		}
		//intializing the connection between client and server
		public void run()
		{
			try {
				//seting up the connection or communction streams with client and server
				setupStreams();
				//function where the commiunication is handled
				whileChatting();
			}
			catch (Exception e){
				
			}
			
			finally{
				//closing the connection once the streams are closed
				try {
					closeCon();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					
				}
			}
		}

//fucntion to initalize the communction strema objects
		private void setupStreams() throws IOException
		{
			//object from sending the msgs from server
			output = new ObjectOutputStream(clientConnection.getOutputStream());
			//making sure everything is sent out from the object
			output.flush();
			//object from listening wht client has to send
			input = new ObjectInputStream(clientConnection.getInputStream());
		}

//decoding the http msg recieved by the client and sending the body of the message as return
	


		//during the chat conversation
		
		private void whileChatting() throws Exception
		{
			//once statring sending the connection authorized msg to client
			String message = " You are now connected! \n";
			//sendMessage(message);
			boolean chek = false;
			String[] splitmsg = null;
			
			//do-while loop to listen or stay connections open 
		
			do
			{
				try
				{
					//geting the input object into string
					message = (String) input.readObject();
					
					if(count>=1)
					{
						
						 splitmsg = username.split("_");
						 if(!username.equalsIgnoreCase("MQS"))
							{
								
								chek = checkMQS();
							}
					}
					
					
					
					//getting username stores as the first intput from the client"Setting username"
					if(count==0)
					{
						username = message;
						count++;
						if(!username.equalsIgnoreCase("MQS"))
						{
						sendMQS("\n"+ username +" joined the network");
						}
						
						else {
							String msgg = " ";
							if(al.size()>1)
							{
							for( int i =al.size();--i>=0;)
							{
								if(!al.get(i).username.equalsIgnoreCase("MQS"))
									msgg = msgg+al.get(i).username+"\n";
							}
							}
							
							if(msgg.isBlank())
							{
								msgg="";
							}
							else {
								msgg= "Members present in the network  :\n"+ msgg;
							}
							sendMessage(msgg);
						}
						//showMessage("\n"+ username +" joined the network");
					
					}
					//checking if MQS is present or not
					else if(!chek)
					{
						//letting others know if its not present
						sendMessage("\n!_MQS is offline please try again later");
					}
					//pushing the messages based on the pull request by advisor
					else if(message.equalsIgnoreCase("get_student_details"))
					{
						
						
						//making the advisor know that its his work by assigning him the request 
						String joined = String.join("\n", Advisor);
						
						if(joined.isEmpty())
						{
							//send msgs to advisor for the pull req reply
							joined="*Hooray!! You have nothing to do now*";
						}
					//sending the message to advisor
						sendMessage(Advisor.size()+"_"+joined);
						
						int j= Advisor.size();
						for(int i=j-1; i>=0;i--)
						{
							//reciveing the message from advisor arraylist as the msg has been pushed to advisor
							 Advisor.remove(i);
							 
						}
					}
					
					
					//send the advisor reply to notification process
					else if(message.equalsIgnoreCase("pull_notification"))
					{
						
						//assign the message to notification
						
						String joined = String.join("\n", Notification);
						
						if(joined.isEmpty())
						{
							//leting it know that there are any msgs left to show
							joined="*No Notification to Show*";
						}
					
						sendMessage(Notification.size()+"_"+joined);
						//once message are pushed to notification they are being deleted from arraylist
						int j= Notification.size();
						for(int i=j-1; i>=0;i--)
						{
							 Notification.remove(i);
							 
						}
					}
					//preparing the reply from the client
					else if(splitmsg[0].equalsIgnoreCase("Student") && !message.equalsIgnoreCase("end"))
					{
						
						Advisor.add(message);
							//showing the message at server chatbox
						int kl = Advisor.size();
						//showMessage("\n"+username+" - " + message);
						
					}		
					else if(splitmsg[0].equalsIgnoreCase("Advisor") && !message.equalsIgnoreCase("end" ))
					{
						
						Notification.add(message);
							//showing the message at server chatbox
						int k = Notification.size();
						//	showMessage("\n"+username+" - " + message);
						
					}
					
					if(count>1)
					{
					//showMessage("\n"+username+" - "+message);
					}
				}
				catch(ClassNotFoundException classNotFoundException)
				{
					showMessage("The user has sent an unknown object!");
				}

			}while(!message.equalsIgnoreCase("END"));//closing the chat when the msg is recieved "end" 
		}

		//connection closed then closing the inputs and output stream and socket
		public void closeCon() throws IOException
		{
			if(!username.equalsIgnoreCase("MQS"))
			{
				sendMQS("\n"+ username +" connection closed... \n");
			}
			//showMessage("\n "+username +" connection closed... \n");
			try
			{
				//removing the client from arraylist(thread from the array list)
				for( int j =al.size();--j>=0;)
				{
					ClientThread k = al.get(j);
					if(k.username==username)
					{
						al.remove(j);
					}
				}
				output.close(); //Closes the output path to the client
				input.close(); //Closes the input path to the ServerGUI, from the client.
				clientConnection.close(); //Closes the connection between you can the client
			}
			catch(IOException ioException)
			{
				
			}
		}


//sending msgs to the client 
		void sendMessage(String message)
		{
			try{
			//sending msgs thtorugh object streams
				output.writeObject(message);
				output.flush();	
			}
			catch(IOException ioException)
			{
				chatWindow.append("\n ERROR: CANNOT SEND MESSAGE, PLEASE RETRY");
			}
		}
	}






}