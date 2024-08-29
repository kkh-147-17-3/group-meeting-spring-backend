package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.user.CreateUserDto;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDeviceDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDto;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.mapper.UserMapper;
import com.sideprj.groupmeeting.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;
    private final UserMapper mapper;

    @Autowired
    public UserService(UserRepository userRepository, AwsS3Service awsS3Service, UserMapper mapper) {
        this.userRepository = userRepository;
        this.awsS3Service = awsS3Service;
        this.mapper = mapper;
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

        newUser = userRepository.save(newUser);
        return mapper.toGetDto(newUser);
    }


    public GetUserDto get(Long userId) throws ResourceNotFoundException {
        var user = userRepository.findById(userId).orElseThrow(ResourceNotFoundException::new);
        return mapper.toGetDto(user);
    }

    private Optional<GetUserDto> getInfo(
            User.SocialProvider socialProvider,
            String socialProviderId) {
        return userRepository.findBySocialProviderAndSocialProviderId(socialProvider, socialProviderId)
                .map(mapper::toGetDto);
    }

    @Transactional
    public GetUserDto update(Long id, UpdateUserDto updateUserDto) throws IOException, BadRequestException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String profileImgName = user.getProfileImgName();
        if (updateUserDto.profileImg() != null) {
            profileImgName = awsS3Service.uploadImage(null, "meeting-sideproject", "profile" ,updateUserDto.profileImg());
        }

        if (checkNicknameDuplicated(updateUserDto.nickname())){
            throw new BadRequestException("중복된 닉네임입니다.");
        }

        user.setProfileImgName(profileImgName);
        user.setNickname(updateUserDto.nickname() != null ? updateUserDto.nickname() : user.getNickname());

        user = userRepository.save(user);
        return mapper.toGetDto(user);
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
        user = userRepository.save(user);
        mapper.toGetDto(user);
    }
}