package com.personal.project.repository;

import com.personal.project.model.PlaidItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlaidItemRepository extends MongoRepository<PlaidItem, String> {
    Optional<PlaidItem> findByUserId(String userId);
}
