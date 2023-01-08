package ru.practicum.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class StatViewForSpecific {
    private String start;
    private String end;
    private String[] uris;
    private boolean unique;
}
