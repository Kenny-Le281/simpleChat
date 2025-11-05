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
  
  private boolean closed = false;
  
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
  public void handleMessageFromClient (Object msg, ConnectionToClient client) {
	String message = msg.toString();
	
	// handle login
	if (message.startsWith("#login")) {
		String[] parts = message.split(" ");
		
		try {
			// Check if login ID was provided
			if (parts.length < 2) {
				client.sendToClient("ERROR - No login ID specified. Connection aborted.");
				client.close();
				return;
			}
		
			// Check is client already has a login key
			if (client.getInfo("loginKey") != null) {
				client.sendToClient("Error: The client is already logged in. Connection is now closed.");
				client.close();
				return;
			}
			
			// Save the login ID
			String loginID = parts[1];
			client.setInfo("loginKey", loginID);
			System.out.println("Message received: #login " + client.getInfo("loginKey") + " from " + client.getInfo("name") + ".");
			System.out.println(client.getInfo("loginKey") + " has logged on.");
		} catch (IOException e) {
			System.out.println("Error: unable to process login " + e.getMessage());
		}
		return;
	}
	
	
    System.out.println("Message received: " + message + " from " + client.getInfo("loginKey"));
    
    this.sendToAllClients(client.getInfo("loginKey") + "> " + message.toString());
  }
  
  public void handleMessageFromServerUI(String message) {
	  if (message.startsWith("#")) {
		  handleCommand(message);
	  } else {
		  serverUI.display("SERVER MESSAGE> " + message);
		  sendToAllClients("SERVER MESSAGE> " + message);
	  }
  }
  
  private void handleCommand(String command) {
	  if (command.equals("#quit")) {
		  serverUI.display("Server terminated successfully");
		  quit();
	  }
	  else if (command.equals("#stop")) {
		  stopListening();
	  }
	  else if (command.equals("#close")) {
		  try {
			  close();
		  } catch (IOException e) {
			  serverUI.display("Error closing the server: " + e);
		  }
	  }
	  else if (command.startsWith("#setport")) {
		  if (!isClosed()) {
			  serverUI.display("Error: unable to change port because server is not closed");
		  } else {
			  // Need to parse the string to get the port name
			  String[] words = command.split(" ");
			  
			  if (words.length < 2) {
				  serverUI.display("Please include a port number");
			  } else {
				  try {
					  String portString = words[1];
					  int portNumber = Integer.parseInt(portString);
					  setPort(portNumber);
					  serverUI.display("Port number set to " + portNumber);
				  } catch (NumberFormatException e) {
					  serverUI.display("Port must be a valid number");
				  }
					  
			  }
		  }
	  }
	  else if (command.equals("#start")) {
		  if (isListening()) {
			  serverUI.display("The server is already listening");
		  } else {
			  try {
				  listen();
			  } catch (IOException e) {
				  serverUI.display("Server was unable to start: " + e);
			  }
		  }
	  }
	  else if (command.equals("#getport")) {
		  int portNumber = getPort();
		  serverUI.display("Port Number: " + portNumber);
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
  
  public boolean isClosed() {
	  return closed;
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
	closed = false;
    System.out.println
      ("Server listening for clients on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  @Override
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server closes.
   */
  @Override
  protected void serverClosed() {
	  closed = true;
  }
  
  /**
   * Implements hook method for clientConnected.
   */
  @Override
  protected void clientConnected(ConnectionToClient client) {
	  // Need to store the host name because if the client disconnects, it won't remember the host name so the client = null
	  System.out.println("A new client has connected to the server.");
  }
  
  /**
   * Implements hook method for clientDisconnected.
   */
  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
	  System.out.println(client.getInfo("loginKey") + " has disconnected.");
	}
  
  @Override
  synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
      System.out.println(client.getInfo("loginKey") + " has disconnected from the server");
  }

}
//End of EchoServer class
