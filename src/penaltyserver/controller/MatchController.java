package penaltyserver.controller;

import java.util.*;
import penaltyserver.PenaltyServer;
import penaltyserver.model.Player;

public class MatchController {
    private ServerNetwork server;
    private Map<String, Match> activeMatches;
    private Queue<Player> waitingPlayers;
    
    public MatchController(ServerNetwork server) {
        this.server = server;
        this.activeMatches = new HashMap<>();
        this.waitingPlayers = new LinkedList<>();
    }
    
    public void handlePlayerJoinQueue(Player player) {
        waitingPlayers.add(player);
        System.out.println("Player " + player.getName() + " joined queue. Queue size: " + waitingPlayers.size());
        
        // Try to create a match if we have 2 players
        if (waitingPlayers.size() >= 2) {
            createMatch();
        }
    }
    
    private void createMatch() {
        Player player1 = waitingPlayers.poll();
        Player player2 = waitingPlayers.poll();
        
        if (player1 == null || player2 == null) return;
        
        String matchId = UUID.randomUUID().toString();
        Match match = new Match(matchId, player1, player2);
        
        activeMatches.put(matchId, match);
        player1.setMatchId(matchId);
        player2.setMatchId(matchId);
        
        System.out.println("Match created: " + player1.getName() + " vs " + player2.getName());
        
        // Start match
        match.startMatch();
    }
    
    public void handlePlayerChoice(Player player, int zoneChoice) {
        String matchId = player.getMatchId();
        Match match = activeMatches.get(matchId);
        
        if (match != null) {
            match.registerChoice(player, zoneChoice);
        }
    }
    
    public void handlePlayerDisconnect(Player player) {
        String matchId = player.getMatchId();
        if (matchId != null) {
            Match match = activeMatches.get(matchId);
            if (match != null) {
                match.handleDisconnect(player);
                activeMatches.remove(matchId);
            }
        }
        
        // Remove from waiting queue if present
        waitingPlayers.remove(player);
    }
    
    // Inner class representing a match between two players
    private class Match {
        private String matchId;
        private Player player1;
        private Player player2;
        
        private Player currentShooter;
        private Player currentKeeper;
        
        private int player1Score = 0;
        private int player2Score = 0;
        private int currentRound = 1;
        private int maxRounds = 5;
        
        private Integer shooterChoice = null;
        private Integer keeperChoice = null;
        
        public Match(String matchId, Player player1, Player player2) {
            this.matchId = matchId;
            this.player1 = player1;
            this.player2 = player2;
        }
        
        public void startMatch() {
            // Randomly choose who shoots first
            if (Math.random() < 0.5) {
                currentShooter = player1;
                currentKeeper = player2;
            } else {
                currentShooter = player2;
                currentKeeper = player1;
            }
            
            // Notify both players
            server.sendToPlayer(player1, "MATCH_START|" + player2.getName() + "|" + currentShooter.getName());
            server.sendToPlayer(player2, "MATCH_START|" + player1.getName() + "|" + currentShooter.getName());
            
            System.out.println("Match started: " + currentShooter.getName() + " shoots first");
            
            // Start first turn after delay
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            startTurn();
        }
        
        private void startTurn() {
            shooterChoice = null;
            keeperChoice = null;
            
            // Notify shooter
            server.sendToPlayer(currentShooter, "TURN_START|" + currentRound + "|SHOOTER");
            
            // Notify keeper
            server.sendToPlayer(currentKeeper, "TURN_START|" + currentRound + "|GOALKEEPER");
            
            System.out.println("Round " + currentRound + ": " + currentShooter.getName() + " shoots, " + currentKeeper.getName() + " keeps");
            
            // Start timer for choices (10 seconds)
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Auto-submit random choices if not received
                    synchronized (Match.this) {
                        if (shooterChoice == null) {
                            shooterChoice = (int)(Math.random() * 6);
                            System.out.println(currentShooter.getName() + " timeout - random zone: " + shooterChoice);
                        }
                        if (keeperChoice == null) {
                            keeperChoice = (int)(Math.random() * 6);
                            System.out.println(currentKeeper.getName() + " timeout - random zone: " + keeperChoice);
                        }
                        
                        if (shooterChoice != null && keeperChoice != null) {
                            processTurn();
                        }
                    }
                }
            }, 10000);
        }
        
        public synchronized void registerChoice(Player player, int zone) {
            if (player.equals(currentShooter)) {
                if (shooterChoice == null) {
                    shooterChoice = zone;
                    System.out.println(currentShooter.getName() + " chose zone: " + zone);
                }
            } else if (player.equals(currentKeeper)) {
                if (keeperChoice == null) {
                    keeperChoice = zone;
                    System.out.println(currentKeeper.getName() + " chose zone: " + zone);
                }
            }
            
            // If both choices received, process immediately
            if (shooterChoice != null && keeperChoice != null) {
                processTurn();
            }
        }
        
        private void processTurn() {
            // Determine if goal or save
            boolean isGoal = (shooterChoice != keeperChoice);
            
            // Update score
            if (isGoal) {
                if (currentShooter.equals(player1)) {
                    player1Score++;
                } else {
                    player2Score++;
                }
            }
            
            System.out.println("Result: Shooter zone " + shooterChoice + ", Keeper zone " + keeperChoice + " -> " + (isGoal ? "GOAL" : "SAVE"));
            System.out.println("Score: " + player1.getName() + " " + player1Score + " - " + player2Score + " " + player2.getName());
            
            // Send result to both players
            // Format: TURN_RESULT|shooterZone|keeperZone|isGoal|myScore|opponentScore|shooterName
            String resultP1 = "TURN_RESULT|" + shooterChoice + "|" + keeperChoice + "|" + isGoal + "|" + 
                            player1Score + "|" + player2Score + "|" + currentShooter.getName();
            String resultP2 = "TURN_RESULT|" + shooterChoice + "|" + keeperChoice + "|" + isGoal + "|" + 
                            player2Score + "|" + player1Score + "|" + currentShooter.getName();
            
            server.sendToPlayer(player1, resultP1);
            server.sendToPlayer(player2, resultP2);
            
            // Wait for animation to finish
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Swap shooter and keeper for next turn
            Player temp = currentShooter;
            currentShooter = currentKeeper;
            currentKeeper = temp;
            
            // After both players have had a turn, increment round
            // Player who started second will shoot again = new round starts
            if (currentShooter.equals(player1) ? currentRound > 1 : currentRound >= 1) {
                currentRound++;
            }
            
            // Check if we've completed all rounds
            if (currentRound > maxRounds) {
                checkMatchEnd();
            } else {
                startTurn();
            }
        }
        
        private void checkMatchEnd() {
            // After 5 rounds (10 turns), check winner
            if (player1Score > player2Score) {
                endMatch(player1);
            } else if (player2Score > player1Score) {
                endMatch(player2);
            } else {
                // Tied - sudden death
                System.out.println("Match tied! Going to sudden death...");
                maxRounds++;
                startTurn();
            }
        }
        
        private void endMatch(Player winner) {
            System.out.println("Match ended! Winner: " + winner.getName());
            
            // Notify both players
            server.sendToPlayer(player1, "MATCH_END|" + winner.getName() + "|" + 
                              player1Score + "|" + player2Score);
            server.sendToPlayer(player2, "MATCH_END|" + winner.getName() + "|" + 
                              player2Score + "|" + player1Score);
            
            // Clean up
            player1.setMatchId(null);
            player2.setMatchId(null);
            activeMatches.remove(matchId);
        }
        
        public void handleDisconnect(Player disconnectedPlayer) {
            Player otherPlayer = disconnectedPlayer.equals(player1) ? player2 : player1;
            
            // Notify other player
            server.sendToPlayer(otherPlayer, "OPPONENT_DISCONNECTED");
            
            // End match
            System.out.println("Player " + disconnectedPlayer.getName() + " disconnected from match");
            
            otherPlayer.setMatchId(null);
        }
    }
}