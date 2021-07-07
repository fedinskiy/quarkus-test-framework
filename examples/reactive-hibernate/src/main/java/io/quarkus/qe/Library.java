package io.quarkus.qe;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    public Uni<Response> find(Integer id) {
        return client.withSession(session -> session.find(Book.class, id)
                .map(book -> book == null
                        ? Response.status(Response.Status.NOT_FOUND)
                        : Response.ok(book.getTitle()))
                .map(Response.ResponseBuilder::build));
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
    public Uni<Response> author(Integer id) {
        Mutiny.Session session = client.openSession();
        Uni<String> result = session.createQuery("Select name from Author author where id=:id", String.class)
                .setParameter("id", id)
                .getSingleResultOrNull();
        session.close();
        return result
                .map(name -> name == null
                        ? Response.status(Response.Status.NOT_FOUND)
                        : Response.ok(name))
                .map(Response.ResponseBuilder::build);
    }

    @POST
    @Path("author/{name}")
    public Uni<Response> createAuthor(String name) {
        System.out.println("Creating " + name);
        Author author = new Author();
        author.setName(name);
        return client.withSession(session -> {
            return session.persist(author)
                    .onItem().call(nothing -> {
                        return session.flush();
                    });
        })
                .map(ignored -> Response.status(Response.Status.CREATED).build());
    }

    @DELETE
    @Path("author/{id}")
    public Uni<Void> deleteAuthor(Integer id) {
        return client
                .withSession(session -> session.find(Author.class, id)
                        .flatMap(session::remove)
                        .flatMap(ignored -> session.flush()));
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
