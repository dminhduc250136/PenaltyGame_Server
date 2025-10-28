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
import penaltyserver.model.ClientHandler;
import penaltyserver.model.SessionManager;
import penaltyserver.model.User;
/**
 *
 * @author This PC
 */
public class PenaltyServer {
    private static final int SERVER_PORT = 12346;
    
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("server is running ... ");
            
            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client socket:" + socket); 

                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }

        }catch(IOException e) {
            e.printStackTrace();
        }
    }  
}