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

import message.ChatMessage; // our very own

/**
 *
 * @author Ohjelmistokehitys
 */
public class ServerClientBackEnd implements Runnable {

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    
    public ServerClientBackEnd(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
        
            // Waits for data
            while(true) {
                ChatMessage msg = (ChatMessage)input.readObject();
                ChatServer.broadcastMessage(msg);
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendMessage(ChatMessage msg) {
        try {
            output.writeObject(msg);
            output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
