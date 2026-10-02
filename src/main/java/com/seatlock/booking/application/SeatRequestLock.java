package com.seatlock.booking.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

@Component
public class SeatRequestLock {

    private final ConcurrentHashMap<SeatKey, LockCell> locks = new ConcurrentHashMap<>();

    public <T> T withLocks(String eventId, Collection<String> seatIds, Supplier<T> operation) {
        List<SeatKey> keys = seatIds.stream()
                .distinct()
                .map(seatId -> new SeatKey(eventId, seatId))
                .sorted((first, second) -> {
                    int eventOrder = first.eventId().compareTo(second.eventId());
                    return eventOrder != 0 ? eventOrder : first.seatId().compareTo(second.seatId());
                })
                .toList();
        List<LockReference> references = new ArrayList<>(keys.size());
        List<LockCell> acquired = new ArrayList<>(keys.size());

        try {
            for (SeatKey key : keys) {
                LockCell cell = locks.compute(key, (ignored, current) -> {
                    LockCell selected = current == null ? new LockCell() : current;
                    selected.references++;
                    return selected;
                });
                references.add(new LockReference(key, cell));
                cell.lock.lock();
                acquired.add(cell);
            }
            return operation.get();
        } finally {
            for (int index = acquired.size() - 1; index >= 0; index--) {
                acquired.get(index).lock.unlock();
            }
            for (int index = references.size() - 1; index >= 0; index--) {
                LockReference reference = references.get(index);
                locks.compute(reference.key(), (ignored, current) -> {
                    if (current != reference.cell()) {
                        throw new IllegalStateException("Seat lock registry reference changed unexpectedly");
                    }
                    current.references--;
                    return current.references == 0 ? null : current;
                });
            }
        }
    }

    private record SeatKey(String eventId, String seatId) {
    }

    private record LockReference(SeatKey key, LockCell cell) {
    }

    private static final class LockCell {
        private final ReentrantLock lock = new ReentrantLock(true);
        private int references;
    }
}
