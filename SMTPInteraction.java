/*************************************
 * Filename:  SMTPInteraction.java
 *************************************/
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Open an SMTP connection to mailserver and send one mail.
 *
 */
public class SMTPInteraction {
    /* Socket to the server */
    private Socket connection;

    /* Streams for reading from and writing to socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;

    private static final String CRLF = "\r\n";

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPInteraction object. Create the socket and the 
       associated streams. Initialise SMTP connection. */
    public SMTPInteraction(EmailMessage mailmessage) throws IOException {
        // Open a TCP client socket with hostname and portnumber specified in
        // mailmessage.DestHost and  mailmessage.DestHostPort, respectively.
        connection = new Socket(mailmessage.DestHost, mailmessage.DestHostPort);

	      
        // attach the BufferedReader fromServer to read from the socket and
        // the DataOutputStream toServer to write to the socket
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	      toServer =   new DataOutputStream(connection.getOutputStream());
	
	/* Read one line from server and check that the reply code is 220.
	   If not, throw an IOException. */
       String response = fromServer.readLine();
       System.out.println(response);
       if (!response.startsWith("220")) {
         throw new IOException ("220 reply not recieved from server");
        }
        
         

	/* SMTP handshake. We need the name of the local machine.
	   Send the appropriate SMTP handshake command. */
	    String localhost = (InetAddress.getLocalHost()).getHostName();
  	  //sendCommand(InetAddress.getLocalHost().getHostName());
      //System.out.println("LOCALHOST: " + CRLF);
      String command = "HELO " + localhost + CRLF;
      sendCommand(command,250);
      sendCommand("HELO " +(localhost), 250);
      
	    isConnected = true;

    }

    /* Send message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(EmailMessage mailmessage) throws IOException {
	/* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */
       sendCommand("MAIL FROM: " + mailmessage.Sender  + CRLF, 250);
       sendCommand("RCPT TO: " + mailmessage.Recipient + CRLF, 250);
       sendCommand("DATA" + CRLF , 354);
       sendCommand("SUBJECT: " + (mailmessage) + CRLF, 250);
       sendCommand(mailmessage.Body.toString()+ CRLF,250);
       
    }

    /* Close SMTP connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
	isConnected = false;
	try {
	    sendCommand("QUIT" + CRLF, 221);
	    connection.close();
	} 
  catch (IOException e) {
	    System.out.println("Unable to close connection: " + e);
	    isConnected = true;
	}
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {
	/* Write command to server and read reply from server. */
	  System.out.println("Command to server: " + command + CRLF);
    toServer.writeBytes(command);
    toServer.writeBytes("Subject: ");
 
	/* Check that the server's reply code is the same as the parameter
	   rc. If not, throw an IOException. */
       String message = fromServer.readLine();
       System.out.println("Replies from server: " + message);
       String rcStr = String.valueOf(rc);
       //change rc to string and compare with startWith
       if (!message.startsWith(rcStr)); { //checks if server message is the same as the parameter rc
         //System.out.println("Servers reply codes do not match");
          throw new IOException("Servers reply codes do not match");
        //else 
        //return;
      }
      
    }
}
