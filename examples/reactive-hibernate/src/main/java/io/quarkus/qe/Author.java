package io.quarkus.qe;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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

}
