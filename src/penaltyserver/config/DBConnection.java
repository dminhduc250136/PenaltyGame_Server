/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver.config;


import java.sql.*;
/**
 *
 * @author This PC
 */
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/penalty_game";
    private static final String USER = "root";
    private static final String PASS = "25013615";

    /**
     * @param args the command line arguments
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
