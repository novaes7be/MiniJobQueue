package com.contatodireto.minijobqueue.jobs.worker;

import com.contatodireto.minijobqueue.jobs.queue.JobQueue;
import com.contatodireto.minijobqueue.jobs.status.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JobWorker implements ApplicationRunner {

    private final JobQueue jobQueue;

    @Override
    public void run(ApplicationArguments arguments) throws InterruptedException {
        Thread thread = new Thread( () -> {
            while (true) {
                Optional<String> take = jobQueue.take();

                if (take.isPresent()) {
                    try {
                        jobQueue.updateStatus(take.get(), JobStatus.PROCESSING.name());

                        Thread.sleep(2000);

                        jobQueue.updateStatus(take.get(), JobStatus.DONE.name());
                    } catch (Exception ex) {
                        jobQueue.updateStatus(take.get(), JobStatus.FAILED.name());
                    }

                }
            }
        });
        thread.start();
    }

}
