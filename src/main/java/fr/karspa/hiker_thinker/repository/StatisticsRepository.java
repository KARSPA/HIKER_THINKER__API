package fr.karspa.hiker_thinker.repository;

import fr.karspa.hiker_thinker.dtos.UserStatisticsDTO;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class StatisticsRepository {

    private MongoTemplate mongoTemplate;


    public UserStatisticsDTO getUserStatistics(String userId) {
        Aggregation aggregation = Aggregation.newAggregation(
                // 1. Filtrer les randonnées de l'utilisateur
                Aggregation.match(Criteria.where("ownerId").is(userId)),
                // 2. Grouper toutes les randonnées pour calculer les statistiques
                Aggregation.group()
                        .count().as("hikeCount")
                        .sum("distance").as("totalDistance")
                        .avg("distance").as("averageDistance")
                        .sum("positive").as("totalPositive")
                        .avg("positive").as("averagePositive")
                        .sum("negative").as("totalNegative")
                        .avg("negative").as("averageNegative")
                        .avg("totalWeight").as("averageWeight")
                        .sum(
                                ConditionalOperators.when(Criteria.where("durationUnit").is("jours"))
                                        .thenValueOf(ArithmeticOperators.Multiply.valueOf("duration").multiplyBy(24))
                                        .otherwiseValueOf("duration")
                        ).as("totalDurationHours")
                        .avg(
                                ConditionalOperators.when(Criteria.where("durationUnit").is("jours"))
                                        .thenValueOf(ArithmeticOperators.Multiply.valueOf("duration").multiplyBy(24))
                                        .otherwiseValueOf("duration")
                        ).as("averageDurationHours")
        );

        AggregationResults<UserStatisticsDTO> results =
                mongoTemplate.aggregate(aggregation, "hikes", UserStatisticsDTO.class);

        return results.getUniqueMappedResult();
    }

}
