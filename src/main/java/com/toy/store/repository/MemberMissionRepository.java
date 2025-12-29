package com.toy.store.repository;

import com.toy.store.model.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberMissionRepository extends JpaRepository<MemberMission, Long> {
    List<MemberMission> findByMemberIdAndMissionDate(Long memberId, LocalDate missionDate);

    Optional<MemberMission> findByMemberIdAndMissionDateAndType(Long memberId, LocalDate missionDate,
            MemberMission.MissionType type);
}
