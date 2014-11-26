/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import message.ChatMessage; // our very own

/**
 *
 * @author Ohjelmistokehitys
 */
public class ChatServer {

    static ArrayList<ServerClientBackEnd> clients = new ArrayList();
    
    public static void main(String[] args) {
        
        try {
            // Start the server in to listen port 3010
            ServerSocket server = new ServerSocket(3010);
            // instead of hard coded port this could for example
            // read the value from a configuration file, etc.
            
            // Start to listen and wait connections
            while(true) {
                // Waits here for the client(s)
                Socket temp = server.accept();
                ServerClientBackEnd backEnd = new ServerClientBackEnd(temp);
                clients.add(backEnd);
                Thread t = new Thread(backEnd);
                // Tell JVM this is background thread,
                // so JVM can kill at exit.
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void broadcastMessage(ChatMessage msg) {
        
        // Pass message to all registered clients
        for (ServerClientBackEnd it : clients) {
            it.sendMessage(msg);
        }        
    }
}
