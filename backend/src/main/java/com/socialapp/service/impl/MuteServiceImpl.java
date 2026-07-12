package com.socialapp.service.impl;

import com.socialapp.entity.Mute;
import com.socialapp.entity.User;
import com.socialapp.exception.ApiException;
import com.socialapp.repository.MuteRepository;
import com.socialapp.repository.UserRepository;
import com.socialapp.service.MuteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MuteServiceImpl implements MuteService {

    private final MuteRepository muteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void mute(String muterUsername, String targetUsername) {
        User muter = getUserOrThrow(muterUsername);
        User target = getUserOrThrow(targetUsername);

        if (muter.getId().equals(target.getId())) {
            throw new ApiException("You cannot mute yourself", HttpStatus.BAD_REQUEST);
        }

        if (!muteRepository.existsByMuterIdAndMutedId(muter.getId(), target.getId())) {
            Mute mute = Mute.builder().muter(muter).muted(target).build();
            muteRepository.save(mute);
        }
    }

    @Override
    @Transactional
    public void unmute(String muterUsername, String targetUsername) {
        User muter = getUserOrThrow(muterUsername);
        User target = getUserOrThrow(targetUsername);
        muteRepository.deleteByMuterIdAndMutedId(muter.getId(), target.getId());
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
