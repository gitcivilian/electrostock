package com.electrostock.catalog.repository;

import com.electrostock.catalog.model.Component;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ComponentRepository extends MongoRepository<Component, String> {

    boolean existsByPartNumberNormalized(String partNumberNormalized);

    boolean existsByPartNumberNormalizedAndIdNot(String partNumberNormalized, String id);

    long countByCategoryId(String categoryId);

    long countByCategoryIdAndActiveTrue(String categoryId);
}
