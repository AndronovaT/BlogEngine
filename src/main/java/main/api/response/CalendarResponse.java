package main.api.response;

import lombok.Data;

import java.util.*;

@Data
public class CalendarResponse {
    Set<Integer> years = new HashSet<>();
    Map<String, Integer> posts = new HashMap<>();
}
