package io.quarkus.qe.database;

import java.net.HttpURLConnection;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;
import io.vertx.mutiny.pgclient.PgPool;

@QuarkusScenario
public class MinimalIT {

    static final String USER = "user";
    static final String PASSWORD = "password";
    static final String DATABASE = "amadeus";
    static final int PORT = 5432;

    @Container(image = "quay.io/debezium/postgres:latest", port = PORT, expectedLog = "database system is ready to accept connections")
    static DefaultService database = new DefaultService()
            .withProperty("POSTGRES_USER", USER)
            .withProperty("POSTGRES_PASSWORD", PASSWORD)
            .withProperty("POSTGRES_DB", DATABASE);

    @Inject
    PgPool postgresql;

    @Test
    public void checkDbConnection() {
        postgresql.query("CREATE TABLE books (id SERIAL PRIMARY KEY, name VARCHAR NOT NULL);")
                .execute()
                .onFailure().invoke((Consumer<Throwable>) Assertions::fail)
                .subscribe().with(results -> {
                    System.out.println(results.size());
                    Assertions.assertNotEquals(0, results.size());
                });
    }

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", USER)
            .withProperty("quarkus.datasource.password", PASSWORD)
            .withProperty("quarkus.datasource.jdbc.url",
                    () -> database.getHost().replace("http", "jdbc:postgresql") + ":" + database.getPort() + "/" + DATABASE)
            .withProperty("quarkus.datasource.reactive.url",
                    () -> database.getHost().replace("http", "postgresql") + ":" +
                            database.getPort() + "/" + DATABASE);

    @Test
    public void checkRestConnection() {
        Response response = app.given().get("test/count");
        System.out.println(response.statusCode());
        Assertions.assertEquals("5", response.body().asPrettyString());
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }
}
