package com.socialapp.service;

public interface BlockService {

    void block(String blockerUsername, String targetUsername);

    void unblock(String blockerUsername, String targetUsername);

    boolean isBlocked(String username1, String username2);
}
