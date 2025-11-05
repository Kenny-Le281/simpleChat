// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import ocsf.client.*;

import java.io.*;

import edu.seg2105.client.common.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 
  
  private String loginID;

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
  
  public ChatClient(String loginID, String host, int port, ChatIF clientUI) throws IOException {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
    this.loginID = loginID;
    openConnection();
  }

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    clientUI.display(msg.toString());
    
    
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromClientUI(String message)
  {
    try
    {
    
      if (message.startsWith("#")) {
    	  handleCommand(message);
      }
      
      else {
    	  sendToServer(message);
      }
    }
    catch(IOException e)
    {
      clientUI.display
        ("Could not send message to server.  Terminating client.");
      quit();
    }
  }
  
  private void handleCommand(String command) {
	  if (command.equals("#quit")) {
		  clientUI.display("Client terminated successfully");
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
   * This method terminates the client.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
  
  /**
	 * Implements the hook method called each time an exception is thrown by the client's
	 * thread that is waiting for messages from the server. The method may be
	 * overridden by subclasses.
	 * 
	 * @param exception
	 *            the exception raised.
	 */
  	@Override
	protected void connectionException(Exception exception) {
  		clientUI.display("The server has shut down");
  		quit();
	}
  	
  	/**
	 * Implements hook method called after the connection has been closed. The default
	 * implementation does nothing. The method may be overriden by subclasses to
	 * perform special processing such as cleaning up and terminating, or
	 * attempting to reconnect.
	 */
  	@Override
	protected void connectionClosed() {
  		clientUI.display("Connection is closed");
	}
  	
  	/**
	 * Hook method called after a connection has been established. The default
	 * implementation does nothing. It may be overridden by subclasses to do
	 * anything they wish.
	 */
  	@Override
	protected void connectionEstablished() {
  		try {
  			sendToServer("#login " + loginID);
  		} catch (IOException e) {
  			clientUI.display("Unable to send loginID to the server: " + e.getMessage());
  		}
	}
	
}
//End of ChatClient class
