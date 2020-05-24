package gg.bayes.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import gg.bayes.challenge.entity.Items;
import gg.bayes.challenge.rest.model.HeroItems;

public interface ItemsRepository  extends JpaRepository<Items, String> {
    @Query(name="SELECT item, time_of_event FROM items where name=?1 AND match_id=?2", nativeQuery = true)
    List<Items> getByNameAndMatchId(String heroName, Long matchId);
}
