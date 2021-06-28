package io.quarkus.qe;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.reactive.mutiny.Mutiny;

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
        return client.withSession(session -> session.createQuery("Select title from Book", String.class).getResultList())
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
    @Path("author/")
    public Uni<String> author() {
        return client.withSession(session -> {
            return session.createQuery("Select name from Author", String.class)
                    .getResultList()
                    .map(list -> list.get(0));
        });
    }

    @GET
    @Path("author/{id}")
    public Uni<String> author(Integer id) {
        return client.withSession(session -> {
            return session.createQuery("Select name from Author author where id=:id", String.class)
                    .setParameter("id", id)
                    .getSingleResult();
        });
    }

    @PUT
    @Path("author/{name}")
    public Uni<Void> createAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return client.withSession(session -> {
            return session.persist(author)
                    .onItem().call(nothing -> {
                        return session.flush();
                    });
        });
    }

    @PUT
    @Path("books/{author}/{name}")
    public Uni<Void> createBook(Integer author, String name) {
        Book book = new Book();
        book.setAuthor(author);
        book.setTitle(name);
        return client.withSession(session -> {
            return session.persist(book)
                    .onItem().call(nothing -> {
                        return session.flush();
                    });
        });
    }

    @GET
    @Path("books/author/{name}")
    public Multi<String> search(String name) {
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
