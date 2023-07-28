package hexlet.code;

import hexlet.code.controllers.RootController;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;

public class App {
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8000");
        return Integer.valueOf(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);

    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
            JavalinThymeleaf.init(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    /*
Hostname    dpg-cj1bini7l0ft7nlq1uj0-a
Port    5432
Database    db_roby
Username    db_roby_user
Password    Hf6zFCjJ4UzRvNNXdLsSmDSn1dVdGFRY
Internal Database URL   postgres://db_roby_user:Hf6zFCjJ4UzRvNNXdLsSmDSn1dVdGFRY@dpg-cj1bini7l0ft7nlq1uj0-a/db_roby
External Database URL   postgres://db_roby_user:Hf6zFCjJ4UzRvNNXdLsSmDSn1dVdGFRY@dpg-cj1bini7l0ft7nlq1uj0-a.singapore-postgres.render.com/db_roby
PSQL Command    PGPASSWORD=Hf6zFCjJ4UzRvNNXdLsSmDSn1dVdGFRY psql -h dpg-cj1bini7l0ft7nlq1uj0-a.singapore-postgres.render.com -U db_roby_user db_roby
    */

}
