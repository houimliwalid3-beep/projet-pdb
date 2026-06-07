package org.isfce.pdb.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.isfce.pdb.model.Svg;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheSvgDao implements ISvgDao {

    private final ISvgDao dao;
    private final Map<String, Svg> cache = new HashMap<>();

    public CacheSvgDao(ISvgDao dao) {
        this.dao = dao;
    }

    @Override
    public Optional<Svg> getFromID(String id) {
        if (cache.containsKey(id)) {
            log.debug("Cache hit pour SVG: " + id);
            return Optional.of(cache.get(id));
        }
        Optional<Svg> result = dao.getFromID(id);
        result.ifPresent(svg -> cache.put(id, svg));
        return result;
    }
}