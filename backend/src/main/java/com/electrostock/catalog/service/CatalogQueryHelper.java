package com.electrostock.catalog.service;

import com.electrostock.catalog.model.RecordStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public final class CatalogQueryHelper {

    private CatalogQueryHelper() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static Query buildFilterQuery(
        String search,
        RecordStatus status,
        String categoryId,
        Sort sort,
        String... searchFields
    ) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (status == RecordStatus.ACTIVE) {
            criteriaList.add(Criteria.where("active").is(true));
        } else if (status == RecordStatus.INACTIVE) {
            criteriaList.add(Criteria.where("active").is(false));
        }

        String normalizedCategoryId = trimToNull(categoryId);
        if (normalizedCategoryId != null) {
            criteriaList.add(Criteria.where("categoryId").is(normalizedCategoryId));
        }

        String normalizedSearch = trimToNull(search);
        if (normalizedSearch != null && searchFields.length > 0) {
            String escaped = Pattern.quote(normalizedSearch);
            List<Criteria> orCriteria = new ArrayList<>(searchFields.length);
            for (String field : searchFields) {
                orCriteria.add(Criteria.where(field).regex(escaped, "i"));
            }
            criteriaList.add(new Criteria().orOperator(orCriteria));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }
        if (sort != null) {
            query.with(sort);
        }
        return query;
    }
}
