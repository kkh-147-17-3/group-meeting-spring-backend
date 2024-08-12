package com.sideprj.groupmeeting.service;

import com.sideprj.groupmeeting.dto.user.CreateUserDto;
import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDeviceDto;
import com.sideprj.groupmeeting.dto.user.UpdateUserDto;
import com.sideprj.groupmeeting.entity.User;
import com.sideprj.groupmeeting.exceptions.ResourceNotFoundException;
import com.sideprj.groupmeeting.mapper.UserMapper;
import com.sideprj.groupmeeting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import com.sideprj.groupmeeting.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AwsS3Service awsS3Service;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate_NewUser_Success() throws BadRequestException {
        CreateUserDto dto = new CreateUserDto(User.SocialProvider.APPLE, "123456");
        User newUser = new User();
        newUser.setId(1L);
        newUser.setSocialProviderId("123456");
        newUser.setSocialProvider(User.SocialProvider.APPLE);

        when(userRepository.findBySocialProviderAndSocialProviderId(
                eq(User.SocialProvider.APPLE), eq("123456"))
        ).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        when(mapper.toGetDto(eq(newUser))).thenReturn(new GetUserDto(1L, null, null, 0));

        GetUserDto result = userService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreate_ExistingUser_ThrowsException() {
        CreateUserDto dto = new CreateUserDto(User.SocialProvider.APPLE, "123456");
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setSocialProvider(User.SocialProvider.APPLE);
        existingUser.setSocialProviderId("123456");

        when(userRepository.findBySocialProviderAndSocialProviderId(
                eq(User.SocialProvider.APPLE), eq("123456"))
        ).thenReturn(Optional.of(existingUser));
        when(mapper.toGetDto(existingUser)).thenReturn(new GetUserDto(1L, null,null, 0));

        assertThrows(BadRequestException.class, () -> userService.create(dto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGet_ExistingUser_Success() throws ResourceNotFoundException {
        User user = new User();
        user.setId(1L);
        user.setNickname("TestUser");
        user.setProfileImgName("TestImg");
        user.setBadgeCount(10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toGetDto(eq(user))).thenReturn(new GetUserDto(1L,"TestUser",user.getProfileImgUrl(), 10));

        GetUserDto result = userService.get(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("TestUser", result.nickname());
    }

    @Test
    void testGet_NonExistingUser_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.get(1L));
    }

    @Test
    void testUpdate_ExistingUser_Success() throws IOException, BadRequestException {
        User user = new User();
        user.setId(1L);
        user.setNickname("OldNickname");

        UpdateUserDto updateDto = new UpdateUserDto(null, "NewNickname");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(mapper.toGetDto(eq(user)))
                .thenReturn(new GetUserDto(1L, "NewNickname", null, 0));

        GetUserDto result = userService.update(1L, updateDto);


        assertNotNull(result);
        assertEquals("NewNickname", result.nickname());

        verify(userRepository).save(user);
    }

    @Test
    void testUpdate_WithProfileImage_Success() throws IOException, BadRequestException {
        User user = new User();
        user.setId(1L);
        user.setNickname("OldNickname");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        UpdateUserDto updateDto = new UpdateUserDto(file, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(awsS3Service.uploadImage(any(), any(), any(), any())).thenReturn("new-image-filename");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(mapper.toGetDto(eq(user)))
                .thenReturn(new GetUserDto(1L, "OldNickname", User.getProfileImgSource("new-image-filename"), 0));

        GetUserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals("OldNickname", result.nickname());
        assertEquals(User.getProfileImgSource("new-image-filename"), result.profileImgUrl());

        verify(awsS3Service).uploadImage(any(), any(), any(), any());
        verify(userRepository).save(user);
    }

    @Test
    void testRemove_AppleUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setSocialProvider(User.SocialProvider.APPLE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.remove(1L);

        verify(userRepository).save(argThat(u -> !u.isActive()));
    }

    @Test
    void testCheckNicknameDuplicated_Exists() {
        when(userRepository.findByNickname("existingNickname")).thenReturn(Optional.of(new User()));

        boolean result = userService.checkNicknameDuplicated("existingNickname");

        assertTrue(result);
    }

    @Test
    void testCheckNicknameDuplicated_NotExists() {
        when(userRepository.findByNickname("newNickname")).thenReturn(Optional.empty());

        boolean result = userService.checkNicknameDuplicated("newNickname");

        assertFalse(result);
    }

    @Test
    void testUpdateDeviceInfo_Success() throws ResourceNotFoundException {
        User user = new User();
        user.setId(1L);

        UpdateUserDeviceDto dto = new UpdateUserDeviceDto("newToken", User.DeviceType.IOS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateDeviceInfo(1L, dto);

        verify(userRepository).save(argThat(u ->
                "newToken".equals(u.getDeviceToken()) &&
                        User.DeviceType.IOS.equals(u.getDeviceType()) &&
                        u.getLastLaunchAt() != null
        ));
    }
}