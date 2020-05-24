package gg.bayes.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import gg.bayes.challenge.entity.Kills;

public interface KillerRepository extends JpaRepository<Kills, String> {
    @Query(name = "SELECT kk.hero AS hero, count(kk.hero) AS count from kills GROUP BY kk.hero", nativeQuery = true)
    List<Kills> fingBy();
}
