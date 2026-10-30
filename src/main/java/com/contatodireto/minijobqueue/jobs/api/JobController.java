package com.contatodireto.minijobqueue.jobs.api;

import com.contatodireto.minijobqueue.jobs.model.Job;
import com.contatodireto.minijobqueue.jobs.queue.JobQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobQueue jobQueue;

    @PostMapping
    public Job createJob (@RequestBody String payload) {
        return  jobQueue.enqueue(payload);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobs(@PathVariable String id) {
        Optional<Job> job = jobQueue.find(id);
        return  job.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
