package com.contatodireto.minijobqueue.jobs.queue;

import com.contatodireto.minijobqueue.jobs.model.Job;
import com.contatodireto.minijobqueue.jobs.status.JobStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobQueue {

    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_NAME = "jobs:queue";


    public Job enqueue(String payload) {
        String id = UUID.randomUUID().toString();

        redisTemplate.opsForHash().putAll("job:" + id, Map.of("payload", payload, "status", JobStatus.PENDING.name()));
        redisTemplate.opsForList().leftPush(QUEUE_NAME, id);

        return new Job(id, payload, JobStatus.PENDING.name());
    }

    public Optional<String> take() {
        String id =  redisTemplate.opsForList().rightPop(QUEUE_NAME, Duration.ofSeconds(5));
        return Optional.ofNullable(id);
    }

    public void updateStatus(String id, String status) {
        redisTemplate.opsForHash().put("job:" + id, "status", status);
    }

    public Optional<Job> find(String id) {
        Map<Object, Object> objectMap = redisTemplate.opsForHash().entries("job:" + id);

        if (objectMap.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new Job(id, (String) objectMap.get("payload"), (String) objectMap.get("status")));
        }
    }

}

