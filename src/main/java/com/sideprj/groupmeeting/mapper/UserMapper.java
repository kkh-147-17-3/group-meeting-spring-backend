package com.sideprj.groupmeeting.mapper;

import com.sideprj.groupmeeting.dto.user.GetUserDto;
import com.sideprj.groupmeeting.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    GetUserDto toGetDto(User entity);
}
