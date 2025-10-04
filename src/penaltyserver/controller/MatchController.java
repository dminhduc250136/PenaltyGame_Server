/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver.controller;

import penaltyserver.model.ClientHandler;
import penaltyserver.model.Match;
import penaltyserver.model.MatchDAO;
import penaltyserver.model.MatchResult;
import penaltyserver.model.MatchResultDAO;
import penaltyserver.model.PenaltyShotDAO;
import penaltyserver.model.SessionManager;
import penaltyserver.model.UserDAO;
import penaltyserver.model.User;

/**
 *
 * @author This PC
 */
public class MatchController {
    private static MatchDAO matchDAO = new MatchDAO();
    private static MatchResultDAO mrDAO = new MatchResultDAO();
    private static UserDAO userDAO = new UserDAO();
    private static PenaltyShotDAO psDAO = new PenaltyShotDAO();

    
    
    public static void startMatch(String enermyUsername, User selfUser) {
        ClientHandler enermyHandler = SessionManager.getSession(enermyUsername);
        ClientHandler selfHandler = SessionManager.getSession(selfUser.getUsername());
        
        try {
            
            // create match
            Match match = new Match(selfUser.getUserId());
            
            // get id from match
            int matchId = matchDAO.createMatch(selfUser.getUserId());
            
            // create 2 match result for 2 player 
            MatchResult mr1 = new MatchResult(matchId, selfUser.getUserId());
            MatchResult mr2 = new MatchResult(matchId, userDAO.getUserIdByUsername(enermyUsername));
            
            // add player information to matchsult
            mrDAO.addPlayerToMatch(mr1.getMatchId(), mr1.getUserId());
            mrDAO.addPlayerToMatch(mr2.getMatchId(), mr2.getUserId());
            
            // send command to 2 player
            // sent message for 2 player to start game
            enermyHandler.sendMessage("START_MATCH:" + matchId);
            selfHandler.sendMessage("START_MATCH:" + matchId);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
