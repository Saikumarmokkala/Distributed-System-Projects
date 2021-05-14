/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab1
 */

package client;

import javax.swing.*;

import server.ServerGUI.ClientThread;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class ClientGUI  extends JFrame {
	//java awt frame for client display
	private JFrame frame;
	//Area where the messages from server and client self msgs will be displayed
	private JTextArea chatWindow;

	private JTextField logicalClock;
	//textfield for the client to enter his or her msg
	private JTextField userText;
	//button for broadcasting the msg
	private JButton broadCast;
	//button for personal msg (one to one) msg
	private JButton personal;
	//stream from where we will listen msgs from server
	private ObjectInputStream input;
	//stream from where we will send msgs to client
	private ObjectOutputStream output;
	//String variable to save msgs from user input
	private String message ="";
	//String variable to store personal msg
	private String personaly;
	//string variable to store username
	//int variable giving out random number for client gui
	static Random rand = new Random();
	private static String usrname="Client_"+ rand.nextInt(300);
	//string variable to store msgs after decoding/encoding them
	private String msg1="";
	//int variable to keep track of the user name
	private static int count =0;
	//hard coded address of the server
	private static final String serverIP ="localhost";
	//socket for the client
	private Socket clientConnection;

	ClientTime ct = new ClientTime();

	public ClientGUI()
	{



		//name of window
		super(usrname);
		
		//stating the logical clock in the thread
		ct.start();
		
		//label for client window
		JLabel label = new JLabel();
		add(label,BorderLayout.NORTH);
		label.setText("DS Lab 2");

		//field for showing logical clock show
		logicalClock=new JTextField();
		logicalClock.setEditable(false);
		add(logicalClock, BorderLayout.PAGE_START);

		//text field for input
		userText=new JTextField();
		userText.setEditable(false);
		add(userText, BorderLayout.SOUTH);

		//Text area where incoming and outgoing msgs are displayed
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);

		//button for sending one to one connection
		personal = new JButton("One to One");
		add(personal,BorderLayout.WEST);

		//button for broadcasting message 
		broadCast = new JButton("BroadCast");
		add(broadCast,BorderLayout.EAST);

		//setting size of jframe
		setSize(500,400);

		//setting location for window
		setLocation(0,0);

		//making the client window visible 
		setVisible(true);

		//gets what ever text is typed after pressing enter 
		userText.addActionListener
		(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{


				//first input will be always user name
				if(count==0)
				{

					count++;
					userText.setText("");
					showMessage("*Your input : "+ e.getActionCommand() +"*");
					sendMessage(e.getActionCommand());
				}
				else 
				{//if input is empty send blank so that server doenst give error and later inform client
					if(!e.getActionCommand().isEmpty())
					{
						sendMessage(e.getActionCommand());
						showMessage("*Your input : "+ e.getActionCommand()+"*");
						userText.setText("");
					}
					else 
					{
						//if no other cases just take the input and send into the server
						showMessage("*Your input : "+ e.getActionCommand()+"*");
						sendMessage(" ");
						userText.setText("");
					}
				}

			}
		}
				);

		//button for sending one to one communication , will prompt user from net work to send
		personal.addActionListener
		(new ActionListener()
		{	
			public void actionPerformed(ActionEvent e) {
				personaly = userText.getText();
				sendMessage("one to one");

			}
		}
				);

		//button for sending broadcast communication, will send to everyone in network
		broadCast.addActionListener
		(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) {

				//if message is not empty sending whatever user types in
				if(!userText.getText().isEmpty())
				{
					sendMessage(userText.getText());
					userText.setText("");
				}

				//if its empty then sending blank message
				else
				{
					sendMessage(" ");
					userText.setText("");
				}
			}
		}
				);


	}


	//decoding the http msg received by the server and sending out only the msg
	private String decodeHttp(String msg) throws Exception  
	{
		URLDecoder.decode(msg,"UTF-8");
		String msg1="";
		String[] brak= msg.split("/");
		String[] ms= brak[1].split(" ");
		msg1="Status "+ ms[1]+ " " +ms[2];
		return msg1;
	}

	// http encoding of the message into http
	String encodeHttp(String msg) throws UnsupportedEncodingException
	{
		int len = msg.length();
		Date d = new Date();
		String type ="";
		//based on the use select the header for http
		if(msg.equalsIgnoreCase("one to one"))
		{
			type = "GET";
		}
		else
		{
			type ="POST";

		}

		//date varible to add date to the server	
		SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yy'at' hh:mm");
		msg1 = type+" / HTTP/1.0 \r\n" +
				"Host: localhost:7689 \r\n"  +
				"Accept-Language: en-us\r\n" +
				"Date:"+sf.format(d)+"\r\n" +
				"Content-type: text/html\r\n"+
				"Content-Length: "+ len +"\r\n"+
				"Body:"+msg;
		//encoding the whole msg into UTF-8 format
		URLEncoder.encode(msg1,"UTF-8");
		//sending back the msg so that it can be sent through the output stream
		return msg1;
	}


	
	//for sending random messages with logical clock value
	public void choiceRandom(String[] list) throws IOException 
	{ 
		//random number to choose randomc lient
		Random ren = new Random();
		
		//getting logical clock value
		int cn = ct.getCount();
	
		//arraylist to store the usernames of presently  client connected
		ArrayList<String> kem = new ArrayList<String>();
		
		//storing the required username of clients exculding ours to an array list
		for(int i =1;i<list.length;i++)
		{
			if(!list[i].equalsIgnoreCase(usrname))
			{
				kem.add(list[i]);
			}
		}

		//size of the arraylist
		int g = kem.size();
		
		//if there are client connected at present other then itself it wont send message
		if(g>0)
		{
			//encoding the message with http with out body consisting of client username and logical time
			String lmsg = encodeHttp("unicast-"+kem.get(ren.nextInt(g))+"-"+usrname +"- Logical Time "+Integer.toString(cn));
			//send the message to server
			direct(lmsg);
		}
	}

	// for letting user choose other users in network while sending one to one.
	public void  ChoiceExample(String[] list)
	{  
		//creating a new frame to display drop down from where user can select othe users in netwrk to send mesage
		Frame f = new Frame();  
		Choice c= new Choice(); 
		//disabling the buttons so the user has option of only selecting the user to send and none other at his disposal
		broadCast.setEnabled(false);
		personal.setEnabled(false);
		userText.setEnabled(false);
		c.setBounds(50,50, 100,100);
		c.add("Select from network Users list below");  
		for(int m=1;m<list.length;m++)
		{
			//removing client self username from the list which is shown to client to select people from network for one to one
			if(!list[m].equalsIgnoreCase(usrname))
			{
				c.add(list[m]);
			}

		}
		//adding choice to the frame
		f.add(c);  

		//setting size of the frame
		f.setSize(200,100);  
		f.setLayout(null);  
		f.setVisible(true);
		//Function which will note the selection for the user in one to one scenario and send to server after encoding it
		c.addItemListener(  (ItemListener) new ItemListener(){  
			public void itemStateChanged(ItemEvent e) 
			{
				String option  =(String) e.getItem();
				String lmsg;
				try
				{
					lmsg = encodeHttp("one2one-"+option+"-"+usrname +"-"+personaly);
					direct(lmsg);
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				} 
				//closing the gui from choice
				f.dispose();
				//enabling all the functions back again
				broadCast.setEnabled(true);
				personal.setEnabled(true);
				userText.setEnabled(true);
				userText.setText("");	
			}  
		});

	}

	//Initiating the client , this fuction is being called by clientMain class
	public void startClient() throws Exception
	{
		try
		{
			// fucntion to create the socket and make the necessary arrangments for connection
			connectToServer();
		
			//function to setup the streams where we can listen and talk to the server by creating streams on the socket
			setUpStreams();
			
			//the fuction which handles the communication between server and client
			whileChatting();
		}
		catch (EOFException e)
		{
			//if eroor the closing the client connection and displaying the msg
			showMessage("\n Client Terminated");
		}
		catch(IOException ei)
		{
			ei.printStackTrace();
		}
		finally 
		{
			//after above steps closing the connection on exit 
			close();
		}
	}

	//funcction to handle the meesages sent by server with logical clock and does the needed changes to the logical clock
	private String Process(String[] m)
	{
		String l= m[1]+m[2];
		showMessage(l);
		String[] g = m[2].split(" ");
		//getting the clock value of the reciever client
		int k = Integer.parseInt(g[4]);
		//geting present client clock value
		int u = ct.getCount();
		if(k>u)
		{
			ct.setCount(k++);
			String me = "* Adjustment was needed in the logical clock! \n Time changed from "+u +" to " + k +" *";
			showMessage(me);
		}
		else {
			showMessage("* Adjustment was not nedded in the logical clock! *");
		}
		
		return l;
	}
	//decoding the msg from server
	private String decoderHttp(String msg) throws Exception  
	{

		//decoding in utf-8
		URLDecoder.decode(msg,"UTF-8");
		String msg1 = null;
		//Splitting the message based on the end carriage value 
		String[] brak= msg.split("\n");
		String[] ms= brak[6].split(":");
		try{

			String[] mg = ms[1].split("-");
			
			//handling messages with logical clock sent via unicast method
			if(mg[0].equalsIgnoreCase("un"))
			{
				Process(mg);
				msg1="";
			}
			
			//all other messages
			else {
				msg1=ms[1];
			}
		}
		catch(Exception e)
		{
			return ("");
		}
		return msg1;
	}

	//Function which creates connection for client to server
	private void connectToServer() throws IOException
	{
		showMessage(" Trying to connect at port 7689 \n");
		//socket being created on the hard coded port value
		clientConnection = new Socket("localhost",7689);


	}
	//after connection is made setting up the communiction ports
	private void setUpStreams() throws IOException
	{
		//output stream which let us to send our msgs over server
		output = new ObjectOutputStream(clientConnection.getOutputStream());
		//making sure all the material on the output object is being sent to server
		output.flush();
		sendMessage(usrname);
		showMessage(usrname+" connected to Server \n");
		//input stream which lets us hear what server is replying back to us
		input = new ObjectInputStream(clientConnection.getInputStream());
	}

	//Function handling the communication between client and server
	private void whileChatting() throws  Exception
	{
		String msg="";
		//if communication is open then only we should allow user to type in the text field
		ableToType(true);
		//first initiate without condition
		do 
		{
			try
			{
				//hear what server is replying
				message=(String) input.readObject();
			
				//checking what type of message client is sending
				String[] http = message.split("/");
				String[] verify = message.split("-");
				

				//for personal messages , opening the choice user interface
				if(verify[0].equalsIgnoreCase("killo"))
				{
					//Initiating function which open pop up window with names of client present in the network
					ChoiceExample(verify);


				}
				
				//for messaged with logical clock sent via unicast method
				else if(verify[0].equalsIgnoreCase("uni"))
				{
					//Initiating method to deal with send unicast messages to random client with logical time
					choiceRandom(verify);
				}


				// verifying whether the msg is received by server
				else if(http[0].contains("HTTP")) {
					msg = decodeHttp(message);
					
				}
				//checking the header of the msg received by the server
				else if(http[0].contains("POST"))
				{
					
					msg = decoderHttp(message);
					showMessage("\n"+msg);
				}
				
				//displaying the message from server
				else {
					showMessage(" "+message);
				}
			}
			catch(ClassNotFoundException e)
			{
				showMessage("\n please send the message again");
			}
		}while(!message.equalsIgnoreCase("END"));// if user inputs end in any case then the communication will be closed eventually leading to the closure of client connection
	}

	//fucntion to properly close all the client related objects like input,output stream and socket of the client
	@SuppressWarnings("deprecation")
	private void close() 
	{
		showMessage("\n Connection Closed");
		sendMessage("***"+usrname+" Connection closed ***");

		ableToType(false);
		try 
		{
			//closing the streams
			input.close();
			output.close();
			clientConnection.close();
			//closing the thread having the clock
			ct.stop();
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
	}

	// sending msg to server after encoding it in htpp
	private void sendMessage(String message1)
	{
		int county = ct.getCount();
		String time = Integer.toString(county);



		try
		{



			String ms = message1;
			ms = encodeHttp(message1);
			//output.writeObject(message);
			//putting the string as object inside the output stream for server to recieve
			output.writeObject(ms);
			output.flush();

		}

		catch (IOException ex)
		{
			chatWindow.append("\n error in messages");
		}
	}
	//displaying the message on the GUI(chatWindow-text area) the messages are decoded
	private void showMessage(final String s)
	{
		SwingUtilities.invokeLater(
				new Runnable() {
					@Override
					public void run() {
						chatWindow.append("\n "+s);
					}
				}
				);
	}

	//send the msg to directly to server
	private void direct(String g) throws IOException
	{

		output.writeObject(g);
		output.flush();
	}
	//Function which handles text field inability so that its restricted when the communication is lost
	private void ableToType(final boolean b)
	{
		SwingUtilities.invokeLater(
				new Runnable() {
					@Override
					public void run() {
						userText.setEditable(b);
					}
				}
				);
	}
	// used to send msg to clients randomly 
	public void sendMessageUnicast(int c) throws IOException
	{
		String ms = "";
		//encoding the message to retrieve the list of user present at the particular point in the network
		ms = encodeHttp("uni");
		//Writing message to output object
		direct(ms);
	}
	
	
	//thread for creating logical clock
	public class ClientTime extends Thread {

		//random function to initiate the clock with random number
		Random rand1 = new Random();
		//taking the random number into integer variable
		int count = rand1.nextInt(50);

		public void run() {

			//infinte loop to keep on increasing the time
			for(;;) 
			{ 
				try { 
					//making the clock increase by 1 second
					Thread.sleep(1000); 
					count ++; 
					//displaying the clock
					logicalClock.setText("Logical Time : "+ Integer.toString(count) + " ");
					//sending the message after every 8 seconds
					if(count%8==0)
					{
						//calling the fucntion to send unicast message
						sendMessageUnicast(count);

					}


				} catch (InterruptedException | IOException e) { 

					e.printStackTrace(); 
				} 
			}
		}

		//function to get the clock value
		public int getCount()
		{
			return count;
		}

		//function to set the clock value
		public void setCount(int count)
		{
			this.count = count;
		}

	}
}
