package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.CreateUserDto;
import com.sideprj.groupmeeting.dto.GetUserDto;
import com.sideprj.groupmeeting.dto.UpdateUserDto;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
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
        return GetUserDto.fromEntity(newUser);
    }

    public Optional<GetUserDto> getInfo(
            User.SocialProvider socialProvider,
            String socialProviderId) {
        return userRepository.findBySocialProviderAndSocialProviderId(socialProvider, socialProviderId)
                .map(GetUserDto::fromEntity);
    }

    @Transactional
    public GetUserDto update(Long id, UpdateUserDto updateUserDto) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String profileImgName = user.getProfileImgName();
        if (updateUserDto.profileImg() != null) {
            profileImgName = awsS3Service.uploadImage(null, "meeting", "profile" ,updateUserDto.profileImg());
        }
        user.setProfileImgName(profileImgName);
        user.setNickname(updateUserDto.nickname() != null ? updateUserDto.nickname() : user.getNickname());

        userRepository.save(user);
        return GetUserDto.fromEntity(user);
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
}