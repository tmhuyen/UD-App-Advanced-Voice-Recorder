package com.example.udapp;

import org.mindrot.jbcrypt.BCrypt;

public class UserModel {
    private String userId;
    private String username;
    private String password;


    public UserModel() {
    }
    public UserModel(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = hashPassword(password);
    }

    public String getPassword() {
        return password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean checkPassword(String txtPassword) {
        return BCrypt.checkpw(txtPassword, password); // Kiểm tra tính hợp lệ của mật khẩu
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt()); // Băm mật khẩu với salt ngẫu nhiên
    }
}
