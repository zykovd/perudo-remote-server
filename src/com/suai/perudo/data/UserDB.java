package com.suai.perudo.data;

import java.util.HashSet;

public class UserDB {
    private HashSet<UserData> users = new HashSet<>();

    public UserDB(HashSet<UserData> users) {
        this.users = users;
    }

    public UserDB() {
    }

    public HashSet<UserData> getUsers() {
        return users;
    }

    public void addUser(String login, String password) {
        users.add(new UserData(login, password));
    }

    public boolean checkPassword(String login, String password) {
        for (UserData data: users) {
            if (data.getLogin().equals(login)) {
                if (data.getPassword().equals(password)) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean contains(String login) {
        for (UserData data: users) {
            if (data.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

}
