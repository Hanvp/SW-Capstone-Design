package sw.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.capstone.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
