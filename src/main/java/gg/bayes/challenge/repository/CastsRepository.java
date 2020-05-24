package gg.bayes.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import gg.bayes.challenge.entity.Casts;
import gg.bayes.challenge.rest.model.HeroItems;
import gg.bayes.challenge.rest.model.HeroSpells;

public interface CastsRepository extends JpaRepository<Casts, String> {
    @Query(name="SELECT c.casts, c.level FROM CASTS c where name=?1 AND match_id=?2", nativeQuery = true)
    List<Casts> getByNameAndMatchId(String heroName, Long matchId);
}
