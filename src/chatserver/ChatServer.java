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
import message.ChatMessage;
import message.ChatUserListUpdate;

/**
 *
 * @author Ohjelmistokehitys
 */
public class ChatServer {
    
    public static final boolean TESTING = true;

    // Clients have two stages in connection to the server:
    // 1. Registered, the socket is in clients list without a user name.
    // 2. Connected, the client has sent ChatConnect with the use name.
    private static ArrayList<ServerClientBackEnd> clients = new ArrayList();
    
    public static void main(String[] args) {

        try {
            // Start the server in to listen port 3010
            ServerSocket server = new ServerSocket(3010);
            // instead of hard coded port this could for example
            // read the value from a configuration file, etc.
            if (ChatServer.TESTING) {
                System.out.println("SERVER: main Online ");
            }
            // Start to listen and wait connections
            while(true) {
                // Waits here for a client to register (i.e. creates socket)
                Socket temp = server.accept();
                addClientSocketToList(temp);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Broadcasts message to all connected clients i.e. the ones that have sent
     * the ChatConnect message. Clients that are only registered will not have
     * message sent to them.
     * 
     * @param obj the message can be ChatMessage or ChatUserListUpdate
     */
    public static void broadcastMessage(Object obj) {
        
        if (ChatServer.TESTING) {
            System.out.println("SERVER: ("+clients.size()+") broadcastMessage "+obj.toString());
        }

        String name;
        Boolean send;
        
        for (ServerClientBackEnd it : clients) {
            name = it.getUserName();
            send = true; // assuming public message
            if (name != null) {
                // This one is connected
                if (obj instanceof ChatMessage) {
                    ChatMessage msg = (ChatMessage)obj;
                    if (msg.isIsPrivate()) {
                        // This is private, is it for this one?
                        if (!name.equals(msg.getPrivateName())) {
                            send = false;
                        }
                    }
                }
                // So do we send message or not?
                if (send) {
                    it.sendMessage(obj);
                }
            }
            // else { This client is registered, but not connected }
        }        
    }
    
    /**
     * Creates a new server client back end with the socket received as
     * parameter. Then puts it to its own thread.
     * 
     * @param s the socket for the new back end
     */
    public static void addClientSocketToList(Socket s) {
        
        ServerClientBackEnd backEnd = new ServerClientBackEnd(s);

        if (ChatServer.TESTING) {
            System.out.println("SERVER: addClientSocketToList "+s+" "+backEnd);
        }
        clients.add(backEnd);
        
        Thread backThread = new Thread(backEnd);
        backThread.setName("Server Client BET");
        // Tell JVM this is background thread, so JVM can kill
        // it when backEnd is destroyed.
        backThread.setDaemon(true);
        backThread.start();

    }
    
    /**
     * Removes the given client from the clients list.
     * 
     * @param client the client to be removed
     */
    public static void removeClientFromList(ServerClientBackEnd client) {
        if (ChatServer.TESTING) {
            System.out.println("SERVER: removeClient "+
                    client+" "+clients.remove(client));
        } else {
            clients.remove(client);
        }
    }
    
    /**
     * Creates an update message containing names of the currently connected
     * clients and broadcasts it to all connected clients.
     */
    public static void sendClientListUpdate() {
        if (ChatServer.TESTING) {
            System.out.println("SERVER: Send list of clients");
        }
        ArrayList<String>names = new ArrayList();
        for (ServerClientBackEnd it : clients) {
            String name = it.getUserName();
            if (name != null) {
                names.add(name);
            }
            // null is client that has not connected yet with user name
        }   
        broadcastMessage(new ChatUserListUpdate(names));
    }
    
    /**
     * The server does not allow multiple clients with same name thus
     * an index number is added to the names of the clients connecting
     * after the first of that name.
     * 
     * @param name the name to be validated
     * @return String containing validated name
     */
    public static String validateUserName(String name) {
        int nameModifier = 0;
        String candidate = name;
        boolean again = false;

        do {
            again = false;
            for (ServerClientBackEnd it : clients) {
                String itName = it.getUserName();
                if (itName != null && itName.equals(candidate)) {
                    nameModifier++;
                    candidate = name + Integer.toString(nameModifier);
                    again = true;
                    break;
                }
            }
        } while (again);
        
        if (ChatServer.TESTING) {
            System.out.println("SERVER: Validate name "+name+" -> "+candidate);
        }
        
        return candidate;
    }
}
