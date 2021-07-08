package io.quarkus.qe;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

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
                    () -> "vertx-reactive:" + database.getHost().replace("http", "postgresql") + ":" + database.getPort() + "/"
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
                .when().get("/library/author/")
                .then()
                .statusCode(200);
    }

    @Test
    public void testJoinSearch() {
        String result = given()
                .when().get("/library/books/author/Kahneman")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertTrue(result.contains("Attention and Effort"));
        Assertions.assertTrue(result.contains("Thinking fast and slow"));
    }

    @Test
    public void testAuthor() {
        String result = given()
                .when().get("/library/author/2")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("Vern", result);
    }

    @Test
    public void testCreation() {
        Response post = given()
                .contentType(ContentType.JSON)
                .post("/library/author/Wodehouse");
        Assertions.assertEquals(201, post.statusCode());
        String result = given()
                .when().get("/library/author/5")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("Wodehouse", result);
    }

    @Test
    public void testTooLongName() {
        given()
                .contentType(ContentType.JSON)
                .post("library/author/Subrahmanyakavi")
                .then()
                .statusCode(201);
        String result = given()
                .when().get("/library/author/6")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("Subrahmanyakavi", result);
    }

    @Test
    public void testGeneratedId() {
        given().put("library/books/2/Around_the_World_in_Eighty_Days")
                .then()
                .statusCode(204);
        String result = given()
                .when().get("library/books/author/Vern")
                .then()
                .statusCode(200)
                .extract().body().asString();
        Assertions.assertEquals("[Around_the_World_in_Eighty_Days]", result);
    }

    @Test
    public void deletion() {
        given().delete("library/author/1")
                .then()
                .statusCode(204);
        Response response = given()
                .when().get("/library/author/1");
        Assertions.assertEquals(404, response.statusCode());
    }
}
