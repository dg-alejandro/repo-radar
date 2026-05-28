package com.reporadar.model;

public class AuthRequest {
    private String name;
    private String email;
    private String password;

    //constructor para login (sin name)
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    //constructor para registro (con name)
    public AuthRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
