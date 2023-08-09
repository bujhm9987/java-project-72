package hexlet.code.models;

import javax.persistence.*;

import hexlet.code.models.query.QUrlCheck;
import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import java.time.Instant;
import java.util.List;

@Entity
public final class Url extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @WhenCreated
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Integer getLastStatusCode() {
        return urlChecks.isEmpty() ? null : urlChecks.get(urlChecks.size() - 1).getStatusCode();
    }

    public Instant getLastCreatedAt() {
        return urlChecks.isEmpty() ? null : urlChecks.get(urlChecks.size() - 1).getCreatedAt();
    }

}
