package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


import java.io.IOException;

import edu.seg2105.client.common.ChatIF;
import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the server.
   */
  ChatIF serverUI;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port, ChatIF serverUI) 
  {
    super(port);
    this.serverUI = serverUI;
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  {
    System.out.println("Message received: " + msg + " from " + client);
    this.sendToAllClients(msg);
  }
  
  public void handleMessageFromServerUI(String message) {
	  if (message.startsWith("#")) {
		  handleCommand(message);
	  } else {
		  sendToAllClients("SERVER MSG> " + message);
	  }
  }
  
  private void handleCommand(String command) {
	  if (command.equals("#quit")) {
		  serverUI.display("Server terminated successfully");
		  quit();
	  }
	  else if (command.equals("#logoff")) {
		  try {
			  closeConnection();
			  clientUI.display("Successfully logged off");
		  } catch (IOException e) {
			  clientUI.display("Failed to log off");
		  }
	  }
	  else if (command.startsWith("#sethost")) {
		  if (isConnected()) {
			  clientUI.display("Error: please log off in order to sethost name");
		  } else {
			  // Need to parse the string to get the host name
			  String[] words = command.split(" ");
			  
			  if (words.length < 2) {
				  clientUI.display("Please include a host name");
			  } else {
				  String hostName = words[1];
				  setHost(hostName);
				  clientUI.display("Host name set to " + hostName);
			  }
		  }
	  }
	  else if (command.startsWith("#setport")) {
		  if (isConnected()) {
			  clientUI.display("Error: please log off in order to set port");
		  } else {
			  // Need to parse the string to get the port name
			  String[] words = command.split(" ");
			  
			  if (words.length < 2) {
				  clientUI.display("Please include a port number");
			  } else {
				  try {
					  String portString = words[1];
					  int portNumber = Integer.parseInt(portString);
					  setPort(portNumber);
					  clientUI.display("Port number set to " + portNumber);
				  } catch (NumberFormatException e) {
					  clientUI.display("Port must be a valid number");
				  }
					  
			  }
		  }
	  }
	  else if (command.equals("#login")) {
		  if (isConnected()) {
			  clientUI.display("Client is already connected");
		  } else {
			  try {
				  openConnection();
				  clientUI.display("Client is logged in to the server");
			  } catch (IOException e) {
				  clientUI.display("Unable to log into the server " + e.getMessage());
			  }
		  }
	  }
	  else if (command.equals("#gethost")) {
		  String hostName = getHost();
		  clientUI.display("Host Name: " + hostName);
	  }
	  else if (command.equals("#getport")) {
		  int portNumber = getPort();
		  clientUI.display("Port Number: " + portNumber);
	  }
	  else {
		  try {
			  sendToServer(command);
		  } catch (IOException e) {
			  clientUI.display("Unable to send message to the server " + e.getMessage());
		  }
	  }
  }
  
  /**
   * This method terminates the server.
   */
  public void quit()
  {
    try
    {
      close();
    }
    catch(IOException e) {}
    System.exit(0);
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  
  /**
   * Implements hook method for clientConnected.
   */
  @Override
  protected void clientConnected(ConnectionToClient client) {
	  // Need to store the host name because if the client disconnects, it won't remember the host name so the client = null
	  String hostName = client.getInetAddress().getHostName();
	  client.setInfo("name", hostName);
	  System.out.println("Client " + hostName + " has connected to the server");
  }
  
  /**
   * Implements hook method for clientDisconnected.
   */
  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
		System.out.println("Client " + client + " has disconnected from the server");
	}
  
  @Override
  synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
	  String hostName = (String) client.getInfo("name");
      System.out.println("Client " + hostName + " has disconnected from the server");
  }


  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
}
//End of EchoServer class
