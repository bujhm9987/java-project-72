package hexlet.code.models;

import javax.persistence.*;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import java.time.Instant;

@Entity
public class Url extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @WhenCreated
    private Instant createdAt;

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

}
