package io.quarkus.qe.hibernate;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.qe.Book;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Library {
    private final Mutiny.SessionFactory client;

    public Library(EntityManagerFactory entityManagerFactory) {
        client = entityManagerFactory.unwrap(Mutiny.SessionFactory.class);
    }

    @GET
    @Path("books")
    public Multi<String> all() {
        return client.withSession(session -> session.createQuery("Select title from Book").getResultList())
                .toMulti()
                .flatMap(list -> Multi.createFrom().iterable(list))
                .onItem().castTo(String.class);
    }

    @GET
    @Path("books/{id}")
    public Uni<String> find(Integer id) {
        return client.withSession(session -> {
            return session.find(Book.class, id)
                    .map(Book::getTitle)
                    .onItem().castTo(String.class);
        });
    }

    @GET
    @Path("author/{id}")
    public Uni<String> author(String id) {
        return client.withSession(session -> {
            return session.createQuery("Select name from Author")
                    .getResultList()
                    .map(list -> list.get(0))
                    .onItem().castTo(String.class);
        });
    }
}
