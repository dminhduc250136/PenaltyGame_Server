/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver.model;


import java.util.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author This PC
 */
public class SessionManager {
    private static Map<String, Socket> onlineUsers = new ConcurrentHashMap<>();

    public static void addSession(String username, Socket socket) {
        onlineUsers.put(username, socket);
    }
    public static void removeSession(String username) {
        onlineUsers.remove(username);
    }
    
    public static List<String> getOnlineUsers() {
        return new ArrayList<>(onlineUsers.keySet());
    }
    public static boolean isOnline(String username) {
        return onlineUsers.containsKey(username);
    }

}
