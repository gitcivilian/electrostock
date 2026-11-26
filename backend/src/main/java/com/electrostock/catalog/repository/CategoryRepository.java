package com.electrostock.catalog.repository;

import com.electrostock.catalog.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, String id);

    boolean existsByNameNormalized(String nameNormalized);

    boolean existsByNameNormalizedAndIdNot(String nameNormalized, String id);
}
