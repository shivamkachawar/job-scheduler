package com.shivam.job_scheduler.scheduling;

import com.shivam.job_scheduler.job.entity.Job;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Condition;
import java.time.Duration;
import java.time.Instant;

@Component
public class JobScheduleQueue {

    private final PriorityQueue<Job> queue = new PriorityQueue<>(Comparator.comparing(Job::getNextRunAt));

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();

    public void add(Job job) {
        lock.lock();
        try {
            queue.offer(job);
            changed.signal();
        } finally {
            lock.unlock();
        }
    }

    public void remove(UUID jobId) {
        lock.lock();
        try {
            queue.removeIf(job -> job.getId().equals(jobId));
            changed.signal();
        } finally {
            lock.unlock();
        }
    }

    public Job peek() {
        lock.lock();
        try {
            return queue.peek();
        } finally {
            lock.unlock();
        }
    }

    public Job poll() {
        lock.lock();
        try {
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    public Job waitUntilNextJobIsDue() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                if (queue.isEmpty()) {
                    changed.await();
                    continue;
                }
                Job job = queue.peek();
                Instant nextRunAt = job.getNextRunAt();

                if (nextRunAt == null) {
                    queue.poll();
                    continue;
                }
                long waitNanos = Duration.between(Instant.now(), nextRunAt).toNanos();
                if (waitNanos <= 0)
                    return queue.poll();
                changed.awaitNanos(waitNanos);
            }
        } finally {
            lock.unlock();
        }
    }

}