package sw.capstone.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.capstone.domain.Member;
import sw.capstone.repository.MemberRepository;
import sw.capstone.service.RootService;
import sw.capstone.web.dto.requestDto.RootRequestDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class RootServiceImpl implements RootService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = false)
    @Override
    public void setDB(RootRequestDto.request request) {
        Member member = Member.builder()
                .id(request.getId())
                .name(request.getName())
                .email(request.getEmail())
                .phoneNum(request.getPhoneNum())
                .build();

        memberRepository.save(member);
    }
}
