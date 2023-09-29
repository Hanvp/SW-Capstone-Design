package sw.capstone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.capstone.domain.RandomNum;

public interface RandomNumRepository extends JpaRepository<RandomNum, Long> {
    Boolean existsByValue(String value);
}
