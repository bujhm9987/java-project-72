package hexlet.code.controllers;

import hexlet.code.models.Url;
import hexlet.code.models.UrlCheck;
import hexlet.code.models.query.QUrl;
import hexlet.code.models.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class UrlController {

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                    .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler addUrl = ctx -> {
        String name = ctx.formParam("url");

        try {
            if (name == null) {
                return;
            }

            URL url = new URL(name);
            String parsingUrl = url.getProtocol() + "://" + url.getAuthority();

            Url urlInDb = new QUrl()
                    .name.equalTo(parsingUrl)
                    .findOne();

            if (urlInDb != null) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/urls");
                return;
            }

            Url newUrl = new Url(parsingUrl);
            newUrl.save();

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");
            ctx.redirect("/urls");

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
        }

    };

    public static Handler showUrl = ctx -> {
        long id = Long.parseLong(ctx.pathParam("id"));

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        List<UrlCheck> urlChecks= new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.desc()
                .findList();

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.render("urls/show.html");
    };

    public static Handler addUrlCheck = ctx -> {
        long id = Long.parseLong(ctx.pathParam("id"));

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            int statusCode = response.getStatus();
            String body = response.getBody();

            Document document = Jsoup.parse(body);
            String title = document.title();
            Element h1Element = document.selectFirst("h1");
            String h1 = h1Element != null ? h1Element.text() : "";
            Element descriptionElement = document.selectFirst("meta[name=description]");
            String description = descriptionElement != null ? descriptionElement.attr("content") : "";

            UrlCheck urlCheck = new UrlCheck(
                    statusCode,
                    title,
                    h1,
                    url,
                    description);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");

        } finally {
            ctx.redirect("/urls/" + id);
        }

    };

}
