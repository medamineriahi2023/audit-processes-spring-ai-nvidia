package com.amaris.auditspringaiollama.models;


public class GatewayInfo {
    private final String id;
    private final String name;

    public GatewayInfo(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}