/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package penaltyserver.model;

/**
 *
 * @author This PC
 */
public class User {
    private int userId;
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }   

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    
    public String getUsername() {
        return this.username;
    }
    public String getPassword() {
        return this.password;
    }
    public void setUsername(String newUsername) {
        this.username = newUsername;
    }
    public void setPassword(String newPassword) {
        this.username = newPassword;
    }

    
}
