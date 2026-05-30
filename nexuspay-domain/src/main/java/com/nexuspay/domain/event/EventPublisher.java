package com.nexuspay.domain.event;

import java.util.ArrayList;
import java.util.List;

public class EventPublisher {
    
    private static final ThreadLocal<List<DomainEvent>> events = ThreadLocal.withInitial(ArrayList::new);
    
    public static void publish(DomainEvent event) {
        events.get().add(event);
    }
    
    public static List<DomainEvent> drainEvents() {
        List<DomainEvent> result = new ArrayList<>(events.get());
        events.get().clear();
        return result;
    }
    
    public static void clear() {
        events.get().clear();
    }
}
