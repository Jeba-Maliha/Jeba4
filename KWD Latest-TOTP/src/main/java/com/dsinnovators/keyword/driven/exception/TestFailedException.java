package com.dsinnovators.keyword.driven.exception;

public class TestFailedException extends Exception {
    public TestFailedException() {
        super("Has not passed expected percentage of testcases.");
    }
}
