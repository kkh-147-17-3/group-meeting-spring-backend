package com.sideprj.groupmeeting.repository;

import com.sideprj.groupmeeting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findBySocialProviderAndSocialProviderId(User.SocialProvider socialProvider, String socialProviderId);

    public Optional<User> findByIdAndActiveTrue(Long id);

    public Optional<User> findByNickname(String nickname);
}
