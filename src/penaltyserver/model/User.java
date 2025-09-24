/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver.model;
import java.io.*;
import java.net.*;
import java.sql.*;
/**
 *
 * @author This PC
 */
public class User {
    private String name;
    private Socket socket;
    private int choice = -1;

    public User(String name, Socket socket) {
        this.name = name;
        this.socket = socket;
    }

    public void setChoice(int choice) { this.choice = choice; }
    public int getChoice() { return choice; }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }
}
