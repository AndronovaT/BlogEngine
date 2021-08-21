package main.api.response;

import lombok.Data;

import javax.persistence.Tuple;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class CalendarResponse {

    Set<Integer> years = new HashSet<>();

    Map<String, Integer> posts = new HashMap<>();


    public CalendarResponse() {
    }

    public CalendarResponse(Integer year, List<Tuple> resultList) {
        if (resultList.isEmpty()) {
            return;
        }

        Map<String, Integer> posts = new HashMap<>();
        Set<Integer> years = new HashSet<>();
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");

        Calendar c = Calendar.getInstance();
        c.set(year, 1, 1);

        resultList.forEach(res -> {
            years.add((Integer) res.get("year"));

            Date timePost = (Date) res.get("time");
            if (timePost.after(c.getTime())) {
                Long countPosts = (Long) res.get("countPosts");
                Integer count = Math.toIntExact(countPosts);
                posts.put(formatDate.format(res.get("time")), count);
            }
        });

        this.years = years;
        this.posts = posts;
    }

}
