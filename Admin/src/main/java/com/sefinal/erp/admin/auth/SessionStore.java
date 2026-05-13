package com.sefinal.erp.admin.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SessionStore {

    public record Session(CurrentUser user, Instant lastSeen) {}

    private final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final Duration idleTimeout;

    public SessionStore(@Value("${app.session.idle-timeout-minutes:30}") long idleMinutes) {
        this.idleTimeout = Duration.ofMinutes(idleMinutes);
    }

    public String create(CurrentUser user) {
        String id = UUID.randomUUID().toString();
        sessions.put(id, new Session(user, Instant.now()));
        return id;
    }

    public Optional<CurrentUser> touch(String id) {
        if (id == null) return Optional.empty();
        Session s = sessions.get(id);
        if (s == null) return Optional.empty();
        if (Duration.between(s.lastSeen(), Instant.now()).compareTo(idleTimeout) > 0) {
            sessions.remove(id);
            return Optional.empty();
        }
        sessions.put(id, new Session(s.user(), Instant.now()));
        return Optional.of(s.user());
    }

    public void invalidate(String id) {
        if (id != null) sessions.remove(id);
    }

}
