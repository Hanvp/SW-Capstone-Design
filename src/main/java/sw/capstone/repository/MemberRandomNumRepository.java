package sw.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.capstone.domain.MemberRandomNum;

public interface MemberRandomNumRepository extends JpaRepository<MemberRandomNum, Long> {
}
