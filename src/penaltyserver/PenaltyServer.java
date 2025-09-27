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
        String username = null;
        try (
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            username = (String) in.readObject();
            String password = (String) in.readObject();
            
            User user = new User(username, password);
            if (AuthController.checkLogin(user)) {
                SessionManager.addSession(username, socket);
                out.writeObject("SUCCESS");
                out.flush();
            } else {
                out.writeObject("FAIL");
                out.flush();
                return; // kết thúc sớm
            }
            
            
            // doc du lieu client gui len
            while(true) {
                String command = (String) in.readObject();
            // lay danh sach online users
            
                if(command.equals("GET_ONLINE_USERS")) {
                    LobbyController.handleSendOnlineUsers(out);
                }
                else if(command.equals("LOGOUT")) {
                    SessionManager.removeSession(username);
                    break;
                }
            }
            

        }
        catch(IOException e) {
            
            if(username != null) {
                SessionManager.removeSession(username);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
}