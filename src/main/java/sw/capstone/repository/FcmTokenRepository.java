package sw.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.capstone.domain.FcmToken;
import sw.capstone.domain.Member;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByFcmToken(String fcmToken);
}
