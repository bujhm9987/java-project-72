package hexlet.code;

import hexlet.code.models.UrlCheck;
import hexlet.code.models.query.QUrlCheck;
import kong.unirest.Empty;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import hexlet.code.models.Url;
import hexlet.code.models.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer mockServer;
    private static String mockUrl;


    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();

        mockServer = new MockWebServer();
        mockUrl = mockServer.url("/").toString();
        String testPage = Files.readString(Paths
                .get("src/test/resources", "testedPage.html"));
        MockResponse mockResponse = new MockResponse().setBody(testPage);
        mockServer.enqueue(mockResponse);
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockServer.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Nested
    @DisplayName("Тестирование страниц")
    class UrlTest {

        @Test
        @DisplayName("Тестирование стартовой страницы index.html")
        void testIndex() {
            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl)
                    .asString();
            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(responseGet.getBody()).contains("Пример: https://www.example.com");
        }

        @Test
        @DisplayName("Тестирование /urls/index.html")
        void testUrlsIndex() {
            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://github.com");
        }

        @Test
        @DisplayName("Тестирование добавления страницы (корректный формат url)")
        void testAddingUrl() {

            String inputUrl = "https://dzen.ru/?yredirect=true";
            String expectedUrl = "https://dzen.ru";

            HttpResponse<Empty> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).contains(expectedUrl);
            assertThat(body).contains("Страница успешно добавлена");

            Url actualUrl = new QUrl()
                    .name.equalTo(expectedUrl)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(expectedUrl);
        }

        @Test
        @DisplayName("Тестирование добавления страницы (некорректный формат url)")
        void testErrorWhenAddingBadUrl() {

            String badUrl = "yandex.ru";

            HttpResponse<Empty> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", badUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(200);

            Url actualUrl = new QUrl()
                    .name.equalTo(badUrl)
                    .findOne();

            assertThat(actualUrl).isNull();
        }
    }

    @Nested
    @DisplayName("Тестирование БД")
    class UrlChecksTest {

        @Test
        @DisplayName("Тестирование /urls/show.html")
        void testUrlsShow() {
            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls/1")
                    .asString();
            String body = responseGet.getBody();

            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://ru.hexlet.io");
            assertThat(body).contains("Проверки");
        }

        @Test
        @DisplayName("Тестирование страницы проверки сайтов")
        void testChecksUrl() {

            HttpResponse<Empty> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", mockUrl)
                    .asEmpty();
            assertThat(responsePost.getStatus()).isEqualTo(302);

            Url urlInDb = new QUrl()
                    .name.equalTo(mockUrl.substring(0, mockUrl.length() - 1))
                    .findOne();
            assertThat(urlInDb).isNotNull();

            HttpResponse<Empty> responsePostChecks = Unirest
                    .post(baseUrl + "/urls/" + urlInDb.getId() + "/checks")
                    .asEmpty();
            assertThat(responsePostChecks.getStatus()).isEqualTo(302);

            UrlCheck checksInDb = new QUrlCheck()
                    .url.equalTo(urlInDb)
                    .createdAt.desc()
                    .setMaxRows(1)
                    .findOne();
            assertThat(checksInDb).isNotNull();

            HttpResponse<String> responseChecks = Unirest
                    .get(baseUrl + "/urls/" + urlInDb.getId())
                    .asString();
            assertThat(responseChecks.getStatus()).isEqualTo(200);

            String body = responseChecks.getBody();

            assertThat(body.contains(checksInDb.getH1())).isTrue();
            assertThat(body.contains(checksInDb.getTitle())).isTrue();
            assertThat(body.contains(checksInDb.getDescription())).isTrue();
            assertThat(body.contains(checksInDb.getUrl().getName())).isTrue();
            assertThat(body.contains(String.valueOf(checksInDb.getStatusCode()))).isTrue();

            Instant createdAt = checksInDb.getUrl().getCreatedAt();
            String formattedCreatedAt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withZone(ZoneId.systemDefault()).format(createdAt);
            assertThat(body.contains(formattedCreatedAt)).isTrue();
        }
    }
}
