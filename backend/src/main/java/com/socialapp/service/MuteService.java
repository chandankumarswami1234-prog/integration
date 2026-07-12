package com.socialapp.service;

public interface MuteService {

    void mute(String muterUsername, String targetUsername);

    void unmute(String muterUsername, String targetUsername);
}
