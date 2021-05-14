/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab1
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
	//arraylist to store the thereads

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
		label.setText("DS Lab 1");
		//text area where msgs will be displayed
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		chatWindow.setEditable(false);
		//seting the size of gui
		setSize(600, 400); //Sets the window size
		setVisible(true);
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

//encoding the msgs (status) and sending it to the client
	String encodeHttp(String s) throws UnsupportedEncodingException 
	{
		String status;
		//if client sends blank msg setting partial content status
		if(s.equalsIgnoreCase("blank"))
		{
			status="206 PatialContent";
			
		}
		else
			//else sending ok msg
			status ="200 OK";
	//date and time to send to client as recipet
		Date d = new Date();
		String msg1="";
		SimpleDateFormat sf = new SimpleDateFormat("yy/MM/dd 'at' hh:mm");
		msg1 = "HTTP/1.0 "+status+" \r\n" +
				"Server : localmachine \r \n"  +
				"Accept-Language: en-us\r\n" +
				"Date:"+sf.format(d)+"\r\n" +
				"Content-type: text/html\r\n";
				
		//encoding the msg in utf-8		
		URLEncoder.encode(msg1,"UTF-8");
		return msg1;
	}

	//endoing the msg to other client as post in http
	String encoderHttp(String mes) throws UnsupportedEncodingException
	{
		int len = mes.length();
		Date d = new Date();
		String msg1 ="";
		
		SimpleDateFormat sf = new SimpleDateFormat("yy/MM/dd 'at' hh:mm");
		msg1 = "POST/ HTTP/1.0 \r\n" +
				"Server: localhost \r\n"  +
				"Accept-Language: en-us\r\n" +
				"Date:"+sf.format(d)+"\r\n" +
				"Content-type: text/html\r\n"+
				"Content-Length: "+ len +"\r\n"+
				"Body:"+mes+" (broadcast)";
		URLEncoder.encode(msg1,"UTF-8");
		return msg1;
	}

	//sending to all the clients on the network
	synchronized void toAll(String message) throws IOException
	{
		//iterating on the array list
		for( int j =al.size();--j>=0;)
		{
			//getting each thread
			ClientThread k = al.get(j);
			//getting the msgs encoded
			String msg1 = encoderHttp(message);
			//writing the msg into client input stream by our output stream
			k.output.writeObject(msg1);
			//making sure everything is sent from the output stream
			k.output.flush();	
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

//send personal msg or one to one msg by taking the username as input to whom msg should  be sent
	boolean personal(String[] s) throws IOException
	{
		
		for( int j =al.size();--j>=0;)
		{
			//getinng thread fromm arraylist
			ClientThread k = al.get(j);
			//matching there username
			if(k.username.equalsIgnoreCase(s[1]))
			{
				//if macthed then sending msg if the msg is not blank
				if(s.length>=4)
				{
					//String msg = ;
					//String msg1 = encodeHttp(msg);
					String msg1 = encoderHttp( ""+s[2]+"- "+s[3] +"(Personal Msg) \n");
					//k.output.writeObject( "\n"+s[2]+"- "+s[3] +"(Personal Msg) \n");
					k.output.writeObject(msg1);
					k.output.flush();
					//showMessage("\n" + s[2]+ " (Personal Msg)");
				}
				else
				{
					//if blank asking client to send the msg again
					showMessage("Blank message recieved , requested sender to send again");
					return false;
				}
			}
		}
		return true;
	}

// class where client thread are handled 
	class ClientThread extends Thread
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
				e.printStackTrace();
			}
			finally{
				//closing the connection once the streams are closed
				closeCon();
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
		private String decodeHttp(String msg) throws Exception  
		{
			URLDecoder.decode(msg,"UTF-8");
			String msg1="";
			String[] brak= msg.split("\n");
			String[] ms= brak[6].split(":");
			msg1=ms[1];
			return msg1;
		}


		//during the chat conversation
		private void whileChatting() throws Exception
		{
			//once statring sending the connection authorized msg to client
			String message = " You are now connected! \n Please enter the username for network";
			sendMessage(message);
			//do-while loop to listen or stay connections open 
			do
			{
				try
				{
					//geting the input object into string
					message = (String) input.readObject();
					//keeping the whole msg intpo a string before decoding so that it can be displayed
					String unDecoded= message;
					//incoming message decoded
					message= decodeHttp(message);
					//showMessage(message);
					String[] killer = message.split("-");
					//if msg recieved is blank notifying the client
					if(message.equalsIgnoreCase(" "))
					{
						sendMessage(" * Server Recieved blank message*");
						sendMessage(encodeHttp("blank"));
					}
					//caliing the specific function if the operation is one to one
					else if(message.equals("one to one"))
					{
						showMessage("\n"+unDecoded);
					}
					else 
						//sending normal msg 
					{
						sendMessage(encodeHttp("normal"));
					}

					//getting username stores as the first intput from the client"Setting username"
					if(count==0)
					{
						username = message;
						count++;
						showMessage("\n"+ username +" joined the network");
					toAll("**"+username +" joined the network***");
					}
					//preapring the reply from the client
					else
					{
						String finMsg=("\n"+ username +"- "+ message);
						String finMsg1=(username +"- "+ message);
						// if message equals end then the connection is closed
						if(message.equalsIgnoreCase("END"))
						{
							finMsg= (" ***"+ username +" left the network ***");
							//showMessage(finMsg);
							////showMessage("\n its going through this loop");
							toAll(finMsg);
						}
						//calling the function whenits one to one call
						else if(message.equalsIgnoreCase("one to one"))
						{ 
							finMsg=getUsers();
							output.writeObject(finMsg);
							output.flush();
						}
						//checking one to one 
						else if(killer[0].equalsIgnoreCase("one2one"))
						{
							if(personal(killer))
							{
								//showMessage(unDecoded);
								//String msg = ;
								//String msg1 = encodeHttp(msg);
								output.writeObject("Recievd sucessfully by "+killer[1]+"\n");
								output.flush();
								showMessage("\n"+unDecoded +"(Personal Msg) \n");
							}
							else 
							{
								//String msg =;
								//String msg1 = encodeHttp(msg);
								output.writeObject("Blank message sent , please send again");
								output.flush();
							}	
						}
						else 
						{
							//showing the message at server chatbox
							showMessage("\n"+username+" - "+unDecoded);
							toAll(finMsg1);
							
						}
					}
				}
				catch(ClassNotFoundException classNotFoundException)
				{
					showMessage("The user has sent an unknown object!");
				}

			}while(!message.equalsIgnoreCase("END"));//closing the chat when the msg is recieved "end" 
		}

		//connection closed then closing the inputs and output stream and socket
		public void closeCon()
		{
			showMessage("\n "+username +" connection closed... \n");
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
				ioException.printStackTrace();
			}
		}


//sending msgs to the client 
		void sendMessage(String message)
		{
			try{
			//sending msgs thtorugh object streams
				output.writeObject("\n"+ message);
				output.flush();	
			}
			catch(IOException ioException)
			{
				chatWindow.append("\n ERROR: CANNOT SEND MESSAGE, PLEASE RETRY");
			}
		}
	}

}