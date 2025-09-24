/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver.controller;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
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
    
}
