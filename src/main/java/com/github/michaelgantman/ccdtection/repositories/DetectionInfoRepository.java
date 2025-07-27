package com.github.michaelgantman.ccdtection.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.github.michaelgantman.ccdtection.dto.DetectionInfoDTO;

public interface DetectionInfoRepository extends MongoRepository<DetectionInfoDTO, UUID> {
	
	List<DetectionInfoDTO> findBySentTimeBetween(Long startTime, Long endTime);

}
