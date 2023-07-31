package hexlet.code.controllers;

import hexlet.code.models.Url;
import hexlet.code.models.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        ctx.render("urls/listurls.html");
    };

    /*public static Handler addUrl = ctx -> {
        //String name = ctx.formParam("name");
        String name = "Igor";

        Url url = new Url(name);


        //url.save();

        //ctx.sessionAttribute("flash", "Страница успешно добавлена");

        ctx.sessionAttribute("flash", url.getName());
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };*/

}
