package main.model.enums;

import org.springframework.data.domain.Sort;

public enum Mode {
    recent, popular, best, early;

    public Sort getSort(){
        switch (this){
            case popular: return Sort.by("commentCount").descending();
            case best: return Sort.by("likeCount").descending();
            case early: return Sort.by("timestamp");
            default: return Sort.by("timestamp").descending();
        }
    }
}
