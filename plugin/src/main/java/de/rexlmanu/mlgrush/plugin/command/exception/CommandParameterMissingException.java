package de.rexlmanu.mlgrush.plugin.command.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

public class CommandParameterMissingException extends Exception {
    public CommandParameterMissingException(String message) {
        super(message);
    }
}
