package com.newrelic.futurestack.istanbul.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.newrelic.futurestack.istanbul.persistence.entity.PipelineData;

@Repository
public interface PipelineDataRepository extends JpaRepository<PipelineData, Long> {

}
