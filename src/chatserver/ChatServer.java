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
import java.util.HashMap;

import message.ChatMessage; // our very own

/**
 *
 * @author Ohjelmistokehitys
 */
public class ChatServer {

    static ArrayList<ServerClientBackEnd> clients = new ArrayList();
    
    // User name array that is sent to the client
    
    public static void main(String[] args) {

        try {
            // Start the server in to listen port 3010
            ServerSocket server = new ServerSocket(3010);
            // instead of hard coded port this could for example
            // read the value from a configuration file, etc.
            
            // Start to listen and wait connections
            while(true) {
                // Waits here for a client to connect (i.e. creates socket)
                Socket temp = server.accept();
                addClient(temp);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void broadcastMessage(ChatMessage msg) {
        
        // Pass message to all connected clients
        for (ServerClientBackEnd it : clients) {
            it.sendMessage(msg);
        }        
    }
    
    public static void addClient(Socket s) {
        ServerClientBackEnd backEnd = new ServerClientBackEnd(s);
        // TESTING
        System.out.println("SERVER: Add client "+backEnd);
        clients.add(backEnd);
        Thread t = new Thread(backEnd);
        // Tell JVM this is background thread, so JVM can kill
        // it when backEnd is destroyed.
        t.setDaemon(true);
        t.start();
    }
    
    public static void removeClient(ServerClientBackEnd client) {
        // TESTING
        System.out.println("SERVER: Remove client "+client);
        clients.remove(client);
    }
}
