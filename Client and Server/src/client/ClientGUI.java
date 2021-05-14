/* Sai Kumar Reddy Mokkala
 * 1001728207
 * DS Lab1
 * git check
 *
 */

package client;

import javax.swing.*;
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
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class ClientGUI  extends JFrame {
//java awt frame for client display
	private JFrame frame;
	//Area where the messages from server and client self msgs will be displayed
	private JTextArea chatWindow;
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
	private String usrname;
	//string variable to store msgs after decoding/encoding them
	private String msg1="";
	//int variable to keep track of the user name
	private static int count =0;
	//hard coded address of the server
	private static final String serverIP ="localhost";
	//socket for the client
	private Socket clientConnection;
	//int variable giving out random number for client gui
	static Random rand = new Random();
	

	public ClientGUI()
	{
		//name of window
		super("Client "+ rand.nextInt(300));
		//label for client window
		JLabel label = new JLabel();
		add(label,BorderLayout.NORTH);
		label.setText("DS Lab 1");

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

		setLocation(0,0);
		//making the client window visible 
		setVisible(true);

		//gets what ever text is typed after pressing enter 
		userText.addActionListener
		(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{

				//showMessage(e.getActionCommand());
				//first input will be always user name
				if(count==0)
				{
					usrname=e.getActionCommand();
					count++;
					userText.setText("");
					showMessage("*Your input : "+ e.getActionCommand() +"*");
					sendMessage(usrname);
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
				if(!userText.getText().isEmpty())
				{
				sendMessage(userText.getText());
				userText.setText("");
				}
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
	
	//decoding the msg from server
	private String decoderHttp(String msg) throws Exception  
	{
		
		URLDecoder.decode(msg,"UTF-8");
		String msg1 = null;
		String[] brak= msg.split("\n");
		String[] ms= brak[6].split(":");
		try{
			msg1=ms[1];
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
		showMessage("Clinet connected to Server \n");
	}
	//after connection is made setting up the communiction ports
	private void setUpStreams() throws IOException
	{
		//output stream which let us to send our msgs over server
		output = new ObjectOutputStream(clientConnection.getOutputStream());
		//making sure all the material on the output object is being sent to server
		output.flush();
		//input stream which lets us hear what server is replying back to us
		input = new ObjectInputStream(clientConnection.getInputStream());
	}

	//Function handling the communication between client and server
	private void whileChatting() throws  Exception
	{
		//if communication is open then only we should allow user to type in the text field
		ableToType(true);
		//first initiate without condition
		do 
		{
			try
			{
				//here what server is replying
				message=(String) input.readObject();
				//message = decodeHttp(message);
				//checking what type of msg mg client is sending
				String[] http = message.split("/");
				String[] verify = message.split("-");
				//showMessage("\n"+http[0]);
				
				//for personal msg , opening the choice gui
				if(verify[0].equalsIgnoreCase("killo"))
				{
					ChoiceExample(verify);
				}
				// verifying whether the msg is received by server
				else if(http[0].contains("HTTP")) {
					String msg = decodeHttp(message);
					//showMessage(msg);
					showMessage("***Server Recieved msg***\n");
				}
				//checking the header of the msg received by the server
				else if(http[0].contains("POST"))
				{
					//showMessage(message);
					String msg = decoderHttp(message);
					showMessage("\n"+msg);
					//showMessage("Server Received msg\n");
					
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
	private void close() 
	{
		showMessage("\n Connection Closed");
		sendMessage("***"+usrname+" Connection closed ***");
		ableToType(false);
		try 
		{
			input.close();
			output.close();
			clientConnection.close();
			//TimeUnit.MINUTES.sleep(5);
			//System.exit(0);
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
	}

// sending msg to server after encoding it in htpp
	private void sendMessage(String message1)
	{
		try
		{
			String ms = message1;
			//showMessage(message1);
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
//displaying the message on the GUI(chatWindow-text area) the msgs are decoded
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
//Function which handles textfield inability so that its restricted when the communication is lost
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

}
