package hexlet.code.models;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import java.time.Instant;

@Entity
public class Url extends Model{
    @Id
    long id;
    String name;
    @WhenCreated
    Instant createdAt;

    public Url (String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
