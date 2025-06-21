package com.myapp.common.exceptions;

public class DuplicatePhoneException extends RuntimeException {
    public DuplicatePhoneException(String phone) {
        super("Phone number already exists: " + phone);
    }
} 