package io.quarkus.qe;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.reactive.mutiny.Mutiny;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Library {
    private static final Logger LOGGER = LoggerFactory.getLogger(Library.class);
    private final Mutiny.SessionFactory client;

    public Library(EntityManagerFactory entityManagerFactory) {
        client = entityManagerFactory.unwrap(Mutiny.SessionFactory.class);
    }

    @GET
    @Path("books")
    public Multi<String> all() {
        LOGGER.info("Getting books");
        return client.withSession(session -> session.createQuery("Select title from Book", String.class).getResultList())
                .toMulti()
                .flatMap(list -> Multi.createFrom().iterable(list))
                .onItem().castTo(String.class);
    }

    @GET
    @Path("books/{id}")
    public Uni<String> find(Integer id) {
        LOGGER.info("Getting book " + id);
        return client.withSession(session -> {
            return session.find(Book.class, id)
                    .map(Book::getTitle)
                    .onItem().castTo(String.class);
        });
    }

    @GET
    @Path("author/")
    public Uni<String> author() {
        LOGGER.info("Getting name");
        System.out.println("Getting name");
        return client.withSession(session -> {
            return session.createQuery("Select name from Author", String.class)
                    .getResultList()
                    .map(list -> list.get(0));
        });
    }

    @GET
    @Path("author/{name}")
    public Multi<String> search(String name) {
        System.out.println("Looking for " + name);
        return client
                .withSession(session -> session.createQuery("Select author from Author author where name=:name", Author.class)
                        .setParameter("name", name)
                        .getResultList())
                .toMulti()
                .flatMap(list -> Multi.createFrom().iterable(list))
                .flatMap(author -> Multi.createFrom().iterable(author.getBooks()))
                .map(Book::getTitle);
    }
}
