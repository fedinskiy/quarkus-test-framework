package io.quarkus.qe;

import static javax.persistence.CascadeType.PERSIST;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "authors")
public class Author {
    private static final int REASONABLY_LONG_TEXT = 100;
    @Id
    @GeneratedValue
    private Integer id;

    @NotNull
    @Size(max = REASONABLY_LONG_TEXT)
    private String name;

    @OneToMany(mappedBy = "author", cascade = PERSIST, fetch = FetchType.EAGER)
    private List<Book> books = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        return books;
    }
}
