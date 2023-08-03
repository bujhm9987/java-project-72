package hexlet.code;

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

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    @Test
    @DisplayName("Инициализация тестов")
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Nested
    @DisplayName("Тестирование страниц")
    class RootTest {

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
            assertThat(body).contains("21/02/2022 12:00");
        }

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
    }

    @Nested
    @DisplayName("Тестирование БД")
    class DatabaseTest {

        @Test
        @DisplayName("Тестирование добавления страницы (позитивное)")
        void testPositiveAddUrl() {

            String inputUrl = "https://dzen.ru/?yredirect=true";
            String expectedUrl = "https://dzen.ru";


            HttpResponse responsePost = Unirest
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
        @DisplayName("Тестирование добавления страницы (негативное)")
        void testNegativeAddUrl() {

            String badUrl = "yandex.ru";

            HttpResponse responsePost = Unirest
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
}
