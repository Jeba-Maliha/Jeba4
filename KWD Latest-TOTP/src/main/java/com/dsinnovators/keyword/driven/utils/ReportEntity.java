package com.dsinnovators.keyword.driven.utils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportEntity {
    private String time;
    private String testCaseName;
    private Integer lineNo;
    private String result;
    private String expectedResult;
    private String reason;
    private String comment;
    private String registryKeyValue;
}
