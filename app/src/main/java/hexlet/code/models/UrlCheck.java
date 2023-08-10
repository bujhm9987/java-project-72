package hexlet.code.models;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int statusCode;

    private String title;

    private String h1;

    @ManyToOne
    private Url url;

    @Lob
    private String description;

    @WhenCreated
    private Instant createdAt;

    public UrlCheck(int statusCode, String title, String h1, Url url, String description) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.url = url;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public Url getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
