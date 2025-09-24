/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver;

import java.io.*;
import java.net.*;
import penaltyserver.controller.AuthController;
import penaltyserver.controller.LobbyController;
import penaltyserver.model.SessionManager;
import penaltyserver.model.User;
/**
 *
 * @author This PC
 */
public class PenaltyServer {
    private static final int SERVER_PORT = 12345;
    
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("server is running ... ");
            
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");

                new Thread(() -> handleClient(socket)).start();
            }

        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    
   
    private static void handleClient(Socket socket) {
        try (
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            String username = (String) in.readObject();
            String password = (String) in.readObject();
            
            User user = new User(username, password);
            boolean checkLogin = AuthController.checkLogin(user);
            SessionManager.addSession(user.getUsername(), socket);
            out.writeObject(checkLogin ? "SUCCESS" : "FAILD");
            
            // doc du lieu client gui len
            
            String command = (String) in.readObject();
            // lay danh sach online users
            
            if(command.equals("GET_ONLINE_USERS")) {
                LobbyController.handleSendOnlineUsers(out);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
}