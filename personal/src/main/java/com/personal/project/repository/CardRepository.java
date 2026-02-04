package com.personal.project.repository;

import com.personal.project.model.Card;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends MongoRepository<Card, String> {
    List<Card> findByUserId(String userId);

    Optional<Card> findByIdAndUserId(String id, String userId);
}
