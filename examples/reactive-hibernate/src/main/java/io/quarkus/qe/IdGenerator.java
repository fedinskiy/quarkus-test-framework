package io.quarkus.qe;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.reactive.id.ReactiveIdentifierGenerator;
import org.hibernate.reactive.session.ReactiveConnectionSupplier;

public class IdGenerator implements ReactiveIdentifierGenerator<Integer> {
    private final AtomicInteger lastId = new AtomicInteger(4);

    @Override
    public CompletionStage<Integer> generate(ReactiveConnectionSupplier session, Object entity) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        result.complete(lastId.incrementAndGet());
        return result;
    }
}
