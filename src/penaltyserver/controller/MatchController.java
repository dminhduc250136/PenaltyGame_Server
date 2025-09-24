package penaltyserver.controller;
import java.io.*;
import java.net.*;
import java.util.*;

public class MatchController {
    private static final int PORT = 12345;
    private static final int MAX_TURNS = 5;  // mỗi đội 5 lượt sút

    private PlayerHandler player1, player2;
    private int currentTurn = 0; // 0..9
    private Timer timer;

    public static void main(String[] args) throws IOException {
        new MatchController().start();
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started, waiting for 2 players...");

        Socket s1 = serverSocket.accept();
        player1 = new PlayerHandler(s1, "P1");
        new Thread(player1).start();

        Socket s2 = serverSocket.accept();
        player2 = new PlayerHandler(s2, "P2");
        new Thread(player2).start();

        System.out.println("Both players connected!");
        startMatch();
    }

    // Bắt đầu trận đấu
    private void startMatch() {
        nextTurn();
    }

    // Xử lý từng lượt
    private void nextTurn() {
        if (currentTurn >= MAX_TURNS * 2) {
            broadcast("GAMEOVER");
            return;
        }

        // Xác định ai sút, ai làm GK
        boolean p1Shoots = (currentTurn % 2 == 0); // lượt chẵn: P1 sút
        player1.setShooter(p1Shoots);
        player2.setShooter(!p1Shoots);

        player1.send("ROLE:" + (p1Shoots ? "SHOOTER" : "KEEPER"));
        player2.send("ROLE:" + (p1Shoots ? "KEEPER" : "SHOOTER"));

        broadcast("RESET:");

        startTimer();
    }

    // Đếm ngược 10s, hết giờ random
    private void startTimer() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            int timeLeft = 10;

            @Override
            public void run() {
                broadcast("TIMER:" + timeLeft);
                if (timeLeft <= 0) {
                    timer.cancel();
                    resolveTurn();
                }
                timeLeft--;
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    // Sau khi cả 2 chọn (hoặc hết giờ)
    private void resolveTurn() {
        int shooterChoice = player1.isShooter() ? player1.getChoice() : player2.getChoice();
        int keeperChoice = player1.isShooter() ? player2.getChoice() : player1.getChoice();

        // random nếu chưa chọn
        if (shooterChoice == -1) shooterChoice = new Random().nextInt(6);
        if (keeperChoice == -1) keeperChoice = new Random().nextInt(6);

        boolean goal = shooterChoice != keeperChoice;

        String shooterId = player1.isShooter() ? "P1" : "P2";
        broadcast("RESULT:" + shooterId + ":" + (currentTurn / 2) + ":" + (goal ? "GOAL" : "MISS"));

        // Reset choice
        player1.resetChoice();
        player2.resetChoice();

        currentTurn++;
        nextTurn();
    }

    private void broadcast(String msg) {
        player1.send(msg);
        player2.send(msg);
    }

    // ================== Inner Class ==================
    private class PlayerHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String id;
        private boolean shooter;
        private int choice = -1;

        public PlayerHandler(Socket socket, String id) {
            this.socket = socket;
            this.id = id;
        }

        public void setShooter(boolean shooter) {
            this.shooter = shooter;
        }

        public boolean isShooter() {
            return shooter;
        }

        public int getChoice() {
            return choice;
        }

        public void resetChoice() {
            choice = -1;
        }

        public void send(String msg) {
            out.println(msg);
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("CHOICE:")) {
                        choice = Integer.parseInt(line.split(":")[1]);
                        System.out.println(id + " chọn ô " + choice);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
