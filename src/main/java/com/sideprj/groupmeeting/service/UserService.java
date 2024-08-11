package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.user.CreateUserDto;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDeviceDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDto;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.mapper.UserMapper;
import com.sideprj.groupmeeting.repository.UserRepository;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;

    @Autowired
    public UserService(UserRepository userRepository, AwsS3Service awsS3Service) {
        this.userRepository = userRepository;
        this.awsS3Service = awsS3Service;
    }

    @Transactional
    public GetUserDto create(CreateUserDto registeredUserInfo) throws BadRequestException {
        Optional<GetUserDto> registeredUser = getInfo(
                registeredUserInfo.socialProvider(),
                registeredUserInfo.socialProviderId()
        );

        if (registeredUser.isPresent()) {
            throw new BadRequestException("User already exists");
        }

        User newUser = new User();
        newUser.setSocialProviderId(registeredUserInfo.socialProviderId());
        newUser.setSocialProvider(registeredUserInfo.socialProvider());

        userRepository.save(newUser);
        return UserMapper.INSTANCE.toGetDto(newUser);
    }

    public Optional<GetUserDto> getInfo(
            User.SocialProvider socialProvider,
            String socialProviderId) {
        return userRepository.findBySocialProviderAndSocialProviderId(socialProvider, socialProviderId)
                .map(UserMapper.INSTANCE::toGetDto);
    }

    @Transactional
    public GetUserDto update(Long id, UpdateUserDto updateUserDto) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String profileImgName = user.getProfileImgName();
        if (updateUserDto.profileImg() != null) {
            profileImgName = awsS3Service.uploadImage(null, "meeting-sideproject", "profile" ,updateUserDto.profileImg());
        }
        user.setProfileImgName(profileImgName);
        user.setNickname(updateUserDto.nickname() != null ? updateUserDto.nickname() : user.getNickname());

        userRepository.save(user);
        return UserMapper.INSTANCE.toGetDto(user);
    }

    @Transactional
    public void remove(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (user.getSocialProvider() == User.SocialProvider.APPLE) {
            deleteAppleLoginUser(user.getId());
        }
    }

    private void deleteAppleLoginUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setActive(false);
        userRepository.save(user);
    }

    public boolean checkNicknameDuplicated(String nickname) {
        var user = userRepository.findByNickname(nickname);
        return user.isPresent();
    }

    @Transactional
    public void updateDeviceInfo(Long userId, @Valid UpdateUserDeviceDto dto) throws ResourceNotFoundException {
        var user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        user.setDeviceToken(dto.deviceToken());
        user.setDeviceType(dto.deviceType());
        user.setLastLaunchAt(LocalDateTime.now());
        userRepository.save(user);
        UserMapper.INSTANCE.toGetDto(user);
    }
}