package sw.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.capstone.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByPhoneNum(String targetPhoneNum);
}
