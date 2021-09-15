package org.seng302.leftovers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.seng302.leftovers.entities.MarketplaceCard;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A DTO for modifying a marketplace card
 */
@Getter
@ToString
@EqualsAndHashCode
public class ModifyMarketplaceCardDTO {

    @NotNull
    private MarketplaceCard.Section section = null;
    @NotNull
    private String title = null;
    private String description;
    @NotNull
    private List<Long> keywordIds;

}

