package com.personal.project.dto;

public class TypeBreakdownItem {
    private final String name;
    private final double value;

    public TypeBreakdownItem(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }
}
