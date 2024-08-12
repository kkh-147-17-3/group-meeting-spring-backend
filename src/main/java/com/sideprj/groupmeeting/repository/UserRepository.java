package com.sideprj.groupmeeting.repository;

import com.sideprj.groupmeeting.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialProviderAndSocialProviderId(User.SocialProvider socialProvider, String socialProviderId);

    Optional<User> findByIdAndActiveTrue(Long id);

    Optional<User> findByNickname(String nickname);
}
