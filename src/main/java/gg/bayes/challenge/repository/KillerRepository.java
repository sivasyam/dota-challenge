package gg.bayes.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import gg.bayes.challenge.entity.Kills;
import gg.bayes.challenge.rest.model.HeroKills;

@Repository
public interface KillerRepository extends JpaRepository<Kills, Integer>, CustomKillsRepository {


}
