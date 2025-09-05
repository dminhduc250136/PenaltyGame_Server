/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver;

import java.io.*;
import java.net.*;
import java.sql.*;
import penaltyserver.config.DBConnection;
/**
 *
 * @author This PC
 */
public class PenaltyServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(12345)) {
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
            
            System.out.println("Thong tin nguoi dung da nhap: Username: " + username + ", Password: " + password);

            boolean checkLogin = checkLogin(username, password);

            out.writeObject(checkLogin ? "SUCCESS" : "FAILD");
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static boolean checkLogin(String username, String password) {
        try(Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
        catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
