package com.ruoyi.projectmanagement.common.enums;
public enum TaskType { SUMMARY, EXECUTION; public boolean matches(String value){return name().equals(value);} }
