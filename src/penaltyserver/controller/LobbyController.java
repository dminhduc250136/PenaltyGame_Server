/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver.controller;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import penaltyserver.model.ClientHandler;
import penaltyserver.model.SessionManager;

/**
 *
 * @author This PC
 */
public class LobbyController {
    
    public LobbyController() {
 
    }
    
    public static void handleSendOnlineUsers(ObjectOutputStream out) throws IOException {
        List<String> onlineUsers = SessionManager.getOnlineUsers();
        out.writeObject(onlineUsers);
        out.flush();

    }
    public static void handleInvite(String targetUser, ClientHandler handler, String username) {
        ClientHandler targetHandler = SessionManager.getSession(targetUser);
        
        
        System.out.println("Invite from " + username + " to " + targetUser);
        System.out.println("Handler(A): " + handler);
        System.out.println("Handler(B): " + targetHandler);
        System.out.println("Equal? " + (handler == targetHandler));
        // neu nguoi choi khong onl gui cho handler moi that bai
        if (targetHandler == null) {
            handler.sendMessage("INVITE_FAIL:");
            System.out.println("Server sent to: " + handler + " " + username + " fail invite from if sence" );
            return;
        }
        try {
            
            // gui thong bao moi cho b
            targetHandler.sendMessage("INVITE_FROM:" + username);
            System.out.println("Server sent to: " + targetHandler + " " + targetUser + " INVITE_FROM from try");
            // neu moi thanh cong tra thong bao cho a
            handler.sendMessage("INVITE_SUCCESS:");
            System.out.println("Server sent to: " + handler + " " + username + " INVITE_SUCCESS from try");
        }
        catch(Exception e) {
            // neu try loi tra fail ve cho a
            handler.sendMessage("INVITE_FAIL:");
            System.out.println("Server sent to handler" + username + "Invite fail from catch ");
        }
    }
    
    public static void handleResponseInvite(String fromUser, String responder, boolean isAccept) {
        ClientHandler fromUserHandler = SessionManager.getSession(fromUser);
        if(fromUserHandler == null) return;
        
        if(isAccept) {
            fromUserHandler.sendMessage("INVITE_RESPONSE_ACCEPT:" + responder);
        }
        else {
            fromUserHandler.sendMessage("INVITE_RESPONSE_DECLINE:" + responder);
        }
    }
}
    