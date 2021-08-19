package org.seng302.leftovers.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * DTO for sending a message between marketplace users.
 */
@Getter
@ToString
public class SendMessageDTO {
    @NotNull
    private Long senderId;
    @NotNull
    private String message;
}
