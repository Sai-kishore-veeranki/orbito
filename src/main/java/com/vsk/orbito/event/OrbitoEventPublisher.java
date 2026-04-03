package com.vsk.orbito.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrbitoEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishPRMerged(PRMergedEvent event) {
        log.info("Publishing PRMergedEvent for PR: {}", event.getPrId());
        eventPublisher.publishEvent(event);
    }

    public void publishTaskAssigned(TaskAssignedEvent event) {
        log.info("Publishing TaskAssignedEvent for task: {}",
                event.getTaskId());
        eventPublisher.publishEvent(event);
    }

    public void publishPRCommentAdded(PRCommentAddedEvent event) {
        log.info("Publishing PRCommentAddedEvent for PR: {}",
                event.getPrId());
        eventPublisher.publishEvent(event);
    }
}