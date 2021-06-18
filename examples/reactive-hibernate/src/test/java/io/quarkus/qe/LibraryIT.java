package io.quarkus.qe;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class LibraryIT {
    private static final String POSTGRES_USER = "quarkus_test";
    private static final String POSTGRES_PASSWORD = "quarkus_test";
    private static final String POSTGRES_DATABASE = "quarkus_test";
    private static final int POSTGRES_PORT = 5432;

    @Container(image = "postgres:10.5", port = POSTGRES_PORT, expectedLog = "database system is ready to accept connections")
    static DefaultService database = new DefaultService()
            .withProperty("POSTGRES_USER", POSTGRES_USER)
            .withProperty("POSTGRES_PASSWORD", POSTGRES_PASSWORD)
            .withProperty("POSTGRES_DB", POSTGRES_DATABASE);

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.username", POSTGRES_USER)
            .withProperty("quarkus.datasource.password", POSTGRES_PASSWORD)
            .withProperty("quarkus.datasource.reactive.url",
                    () -> database.getHost().replace("http", "postgresql") + ":" + database.getPort() + "/"
                            + POSTGRES_DATABASE);

    @Test
    public void testUniEndpoint() {
        String title = given()
                .when().get("/library/books/1")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("Slovník", title);
    }

    @Test
    public void testFind() {
        String title = given()
                .when().get("/library/books/2")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("Thinking fast and slow", title);
    }

    @Test
    public void testMultiEndpoint() {
        String result = given()
                .when().get("/library/books")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertTrue(result.contains("Slovník"));
        Assertions.assertTrue(result.contains("Thinking fast and slow"));
    }

    @Test
    public void testQuery() {
        given()
                .when().get("/library/author/1")
                .then()
                .statusCode(200);
    }
}
