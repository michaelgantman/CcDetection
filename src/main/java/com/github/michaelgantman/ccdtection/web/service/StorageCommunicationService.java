package com.github.michaelgantman.ccdtection.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.github.michaelgantman.ccdtection.dto.DetectionInfoDTO;
import com.github.michaelgantman.ccdtection.dto.SenderCountDTO;
import com.github.michaelgantman.ccdtection.repositories.DetectionInfoRepository;
import jakarta.annotation.Resource;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;


@Service
public class StorageCommunicationService {
	
	@Resource
	private DetectionInfoRepository detectionInfoRepository;
	
	@Value("${mongo.collection.name}")
	private String mongoCollectionName;
	
	@Resource
    private MongoTemplate mongoTemplate;

	public void writeDetectionDoc(DetectionInfoDTO detectionInfoDTO) {
		detectionInfoRepository.save(detectionInfoDTO);
	}

//	public List<DetectionInfoDTO> readDetectionsBetweenTimes(Long startTime, Long endTime) {
//		return detectionInfoRepository.findBySentTimeBetween(startTime, endTime);
//	}
//
    public List<SenderCountDTO> readDetectionsBetweenTimes(Long fromTime, Long toTime) {
        Aggregation aggregation = newAggregation(
            match(
                Criteria.where("sentTime").gte(fromTime).lte(toTime)
            ),
            group("sender").count().as("count"),
            project("count").and("sender").previousOperation()
        );

        AggregationResults<SenderCountDTO> results = mongoTemplate.aggregate(
            aggregation,
            mongoCollectionName,
            SenderCountDTO.class
        );

        return results.getMappedResults();
    }
}
