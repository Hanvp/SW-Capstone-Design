package sw.capstone.domain;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@DynamicUpdate
public class Member {

    @Id
    private Long id;

    private String name;
    @Column(nullable = false)
    private String email;

    @Column(length = 18)
    private String phoneNum;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberNotification> memberNotificationList;
}
