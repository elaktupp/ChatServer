/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import message.ChatConnect;
import message.ChatConnectResponse;
import message.ChatDisconnect;

import message.ChatMessage; // our very own

/**
 *
 * @author Ohjelmistokehitys
 */
public class ServerClientBackEnd implements Runnable {

    private final Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String userName;
    
    public ServerClientBackEnd(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        
            // Waits for data from clients
            while(true) {

                messageHandler(input.readObject());
                
            }
        } catch (SocketException ex) {
            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException ex1) {
                // Nothing we can do...
            }
            ChatServer.removeClientFromList(this);
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Sends the server message to the client represented by this instance.
     * 
     * @param obj the message to be sent
     */
    public void sendMessage(Object obj) {
        
        if (ChatServer.TESTING) {
            System.out.println("SYSTEM BACK: sendMessage "+obj.toString());
        }
        
        try {
            output.writeObject(obj);
            output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Handles different messages sent to the server.
     * 
     * @param obj the message can be ChatConnect, ChatMessage or ChatDisconnect
     */
    private void messageHandler(Object obj) {

        if (ChatServer.TESTING) {
            System.out.println("SYSTEM BACK: messageHandler "+obj.toString());
        }
        
        if (obj instanceof ChatMessage) {
            
            ChatServer.broadcastMessage(obj);
            
        } else if (obj instanceof ChatConnect) {
            
            String validName = ChatServer.validateUserName(((ChatConnect)obj).getUserName());
            setUserName(validName);
            
            // Make response message
            ChatConnectResponse resp = new ChatConnectResponse();
            resp.setConnected(true);
            if (((ChatConnect)obj).getUserName().equals(validName)) {
                resp.setNameChanged(false);
            } else {
                resp.setNameChanged(true);
            }
            resp.setUserName(validName);
            sendMessage(resp);
            
            ChatServer.sendClientListUpdate();
 
        } else if (obj instanceof ChatDisconnect) {
            
            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException ex) {
                // Nothing we can do...
                ex.printStackTrace();
            }
            
            ChatServer.removeClientFromList(this);
            ChatServer.sendClientListUpdate();
            
        } else {
            // Should never happen
            System.out.println("SYSTEM BACK: What was that?");
            System.exit(1);
        }
    }

}
