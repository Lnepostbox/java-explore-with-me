package ru.practicum.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class StatViewForSpecific {
    private String start;
    private String end;
    private List<String> uris;
    private boolean unique;
}
