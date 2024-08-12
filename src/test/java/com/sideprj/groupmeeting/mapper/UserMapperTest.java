package com.sideprj.groupmeeting.mapper;

import com.sideprj.groupmeeting.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UserMapperTest {
    @Test
    void test_ToGetDto(){
        var user = User.builder()
                .id(1L)
                .nickname("testnickname")
                .profileImgName("test-img-name")
                .build();

        var dto = UserMapper.INSTANCE.toGetDto(user);

        assertEquals(dto.id(), user.getId());
        assertEquals(dto.nickname(), user.getNickname());
        assertEquals(dto.profileImgUrl(), user.getProfileImgUrl());
    }
}
