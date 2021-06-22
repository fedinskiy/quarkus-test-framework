package io.quarkus.qe;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "books")
public class Book {
    private static final int REASONABLY_LONG_TEXT = 100;
    @Id
    @GeneratedValue
    private Integer id;

    @NotNull
    @Size(max = REASONABLY_LONG_TEXT)
    private String title;

    @NotNull
    private Integer author;

    public Book(String title) {
        this.title = title;
    }

    public Book() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
