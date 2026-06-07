package org.isfce.pdb.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.isfce.pdb.model.Appareil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheAppareilDao implements IAppareilDao {

    private final IAppareilDao dao;
    private final Map<String, Appareil> cache = new HashMap<>();

    public CacheAppareilDao(IAppareilDao dao) {
        this.dao = dao;
    }

    @Override
    public Optional<Appareil> getFromID(String id) {
        if (cache.containsKey(id)) {
            log.debug("Cache hit pour Appareil: " + id);
            return Optional.of(cache.get(id));
        }
        Optional<Appareil> result = dao.getFromID(id);
        result.ifPresent(appareil -> cache.put(id, appareil));
        return result;
    }
}