package gg.bayes.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import gg.bayes.challenge.entity.Hits;

public interface HitsRepository extends JpaRepository<Hits, String> {
    @Query(name = "SELECT hit, damage_count, h.total_damages FROM hits where name=?1 AND match_id=?2", nativeQuery = true)
    List<Hits> getByNameAndMatchId(String heroName, Long matchId);
}
