package com.amaris.auditspringaiollama.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class EventInfoDto {
    private final String id;
    private final String name;
    private final String type;

}
