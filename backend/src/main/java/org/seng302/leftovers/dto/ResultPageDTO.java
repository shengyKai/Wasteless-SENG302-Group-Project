package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Data Transfer Object for fetching a page of results.
 * @param <T> Page transfer element type (DTO or JSONObject).
 */
@Getter
@ToString
public class ResultPageDTO<T> {
    @NotNull
    private Long count;
    @NotNull
    private List<T> results;

    public ResultPageDTO(Page<T> resultPage) {
        count = resultPage.getTotalElements();
        results = resultPage.getContent();
    }
}
