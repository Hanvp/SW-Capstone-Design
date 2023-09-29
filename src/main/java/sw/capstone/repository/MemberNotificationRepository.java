package sw.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.capstone.domain.Member;
import sw.capstone.domain.MemberNotification;
import sw.capstone.domain.Notification;

import java.util.Optional;

public interface MemberNotificationRepository extends JpaRepository<MemberNotification, Long> {
    Notification findNotificationByMember(Member member);
}
