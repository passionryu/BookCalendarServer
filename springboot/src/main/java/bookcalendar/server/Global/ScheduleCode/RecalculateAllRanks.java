package bookcalendar.server.global.ScheduleCode;

import bookcalendar.server.Domain.Member.Entity.Member;
import bookcalendar.server.Domain.Member.Repository.MemberRepository;
import bookcalendar.server.global.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecalculateAllRanks {

    private final MemberRepository memberRepository;

    @Qualifier("cacheRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    private static final String RANKING_UPDATE_KEY = "ranking:update:memberIds";

    public void addMemberToRankingUpdate(Long memberId) {
        redisTemplate.opsForSet().add(RANKING_UPDATE_KEY, memberId.toString());
    }

    public Set<Long> getMemberIdsToUpdate() {
        Set<String> stringIds = redisTemplate.opsForSet().members(RANKING_UPDATE_KEY);
        redisTemplate.delete(RANKING_UPDATE_KEY);
        return stringIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    /**
     * 랭킹 재배치 스케줄러
     */
    //@Scheduled(fixedRate = 60000) // 60초마다 실행
    @Scheduled(cron = "0 0/5 * * * ?")  // 매 10분마다 실행
    @Transactional
    @CacheEvict(value = "rankCache", allEntries = true)
    public void recalculateAllRanks() {
        log.info("✅ 랭킹 재계산 작업 시작");

        // Redis에서 유저 ID 목록 추출
        Set<String> memberIdStrings = redisTemplate.opsForSet().members(RANKING_UPDATE_KEY);
        if (memberIdStrings == null || memberIdStrings.isEmpty()) {
            log.info("변경된 유저 없음 - 랭킹 재계산 생략");
            return;
        }

        // Redis 큐 초기화
        redisTemplate.delete(RANKING_UPDATE_KEY);

        List<Member> members = memberRepository.findAll(Sort.by(Sort.Direction.DESC, "reviewCount"));
        int total = members.size();

        int i = 0;
        while (i < total) {
            int count = members.get(i).getReviewCount();
            int sameScoreStart = i;

            // 동일한 리뷰 수를 가진 그룹의 마지막 index 찾기
            while (i < total && members.get(i).getReviewCount() == count) {
                i++;
            }

            // 동일 그룹에 대해 같은 랭킹(percentile) 부여
            int percentile = (int) Math.floor((double) sameScoreStart / total * 100);
            for (int j = sameScoreStart; j < i; j++) {
                members.get(j).setRank(percentile);
            }
        }

        log.info("✅ 랭킹 재계산 작업 완료");
    }

}
