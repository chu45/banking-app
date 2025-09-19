package com.example.finance.exceptions;

public class MaximumTransferLimitExceededException extends RuntimeException {
    public MaximumTransferLimitExceededException(String message) {
        super(message);
    }
}
