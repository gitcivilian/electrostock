package com.electrostock.catalog;

import com.electrostock.catalog.model.RecordStatus;
import com.electrostock.catalog.service.CatalogQueryHelper;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogQueryHelperTest {

    @Test
    void buildsEmptyCriteriaForAllStatusWithoutFilters() {
        Query query = CatalogQueryHelper.buildFilterQuery(
            "   ",
            RecordStatus.ALL,
            null,
            Sort.by(Sort.Direction.ASC, "name"),
            "code",
            "name"
        );

        assertThat(query.getQueryObject()).isEmpty();
        assertThat(query.getSortObject()).containsEntry("name", 1);
    }

    @Test
    void buildsCombinedAndCriteriaWithEscapedRegex() {
        Query query = CatalogQueryHelper.buildFilterQuery(
            "10k (0603)",
            RecordStatus.ACTIVE,
            "cat-1",
            Sort.by(Sort.Direction.ASC, "partNumber"),
            "partNumber",
            "name"
        );

        Document queryDoc = query.getQueryObject();
        assertThat(queryDoc.toJson()).contains("\"active\": true");
        assertThat(queryDoc.toJson()).contains("\"categoryId\": \"cat-1\"");
        assertThat(queryDoc.toJson()).contains("\\\\Q10k (0603)\\\\E");
    }
}
