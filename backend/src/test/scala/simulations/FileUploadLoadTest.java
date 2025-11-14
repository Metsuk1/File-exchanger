package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class FileUploadLoadTest extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json");

    Iterator<Map<String, Object>> userFeeder = Stream.generate(() -> {
        Map<String, Object> user = new HashMap<>();
        user.put("email", String.format("loadtest%d_%d@example.com",
                System.currentTimeMillis(), ThreadLocalRandom.current().nextInt(10000)));
        user.put("name", "TestUser" + ThreadLocalRandom.current().nextInt(10000));
        user.put("password", "pass123");
        return user;
    }).iterator();

    ScenarioBuilder scn = scenario("File Upload Load Test")
            .feed(userFeeder)
            .exec(http("Register")
                    .post("/api/v1/users/register")
                    .body(StringBody("{\"name\":\"#{name}\",\"email\":\"#{email}\",\"password\":\"#{password}\"}"))
                    .asJson()
                    .check(status().is(200)))
            .pause(1)
            .exec(http("Login")
                    .post("/api/v1/users/login")
                    .body(StringBody("{\"email\":\"#{email}\",\"password\":\"#{password}\"}"))
                    .asJson()
                    .check(regex("(.+)").saveAs("jwtToken")))
            .pause(1)
            .exec(http("Upload File")
                    .post("/api/v1/files/upload")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .bodyPart(RawFileBodyPart("file", "bodies/test.txt").fileName("test.txt").contentType("text/plain")) //it is the path to the test file
                    .bodyPart(StringBodyPart("fileName", "test.txt"))
                    .asMultipartForm()
                    .check(status().in(200, 201)));

    {
        setUp(
                scn.injectOpen(
                        rampUsers(50).during(Duration.ofSeconds(30))
                ).protocols(httpProtocol)
        ).maxDuration(Duration.ofMinutes(5));
    }
}
