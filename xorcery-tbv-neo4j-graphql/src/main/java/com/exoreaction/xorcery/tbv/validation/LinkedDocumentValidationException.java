package com.exoreaction.xorcery.tbv.validation;

public class LinkedDocumentValidationException extends RuntimeException {

    LinkedDocumentValidationException(String message) {
        super(message);
    }

    LinkedDocumentValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
