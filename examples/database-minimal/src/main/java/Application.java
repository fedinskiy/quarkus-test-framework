import javax.inject.Inject;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

@RouteBase(path = "/test")
public class Application {

    @Inject
    PgPool postgresql;

    @Route(methods = Route.HttpMethod.GET, path = "/count")
    public Uni<String> countDb() {
        return postgresql.query("SELECT * from (VALUES (1),(2),(3),(4),(5)) as result")
                .execute()
                .map(set -> String.valueOf(set.size()))
                .onFailure().recoverWithItem(error -> error.getMessage());
    }
}
