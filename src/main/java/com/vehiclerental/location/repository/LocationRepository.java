package com.vehiclerental.location.repository;

import com.vehiclerental.common.FileRepository;
import com.vehiclerental.location.entity.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

/** Stores pickup / drop-off locations in data/locations.txt */
@Repository
public class LocationRepository extends FileRepository<Location> {

    public LocationRepository(@Value("${app.data-dir:data}") String dataDir) {
        super(Path.of(dataDir, "locations.txt"));
    }

    @Override
    protected String headerComment() {
        return "id|createdAt|name|city|address|phone|active|latitude|longitude";
    }

    @Override
    protected String[] toFields(Location l) {
        return new String[]{text(l.getName()), text(l.getCity()), text(l.getAddress()), text(l.getPhone()),
                String.valueOf(l.isActive()),
                l.getLatitude() == null ? "" : String.valueOf(l.getLatitude()),
                l.getLongitude() == null ? "" : String.valueOf(l.getLongitude())};
    }

    @Override
    protected Location fromFields(String[] f) {
        Location l = new Location();
        l.setName(str(f, 0));
        l.setCity(strOrNull(f, 1));
        l.setAddress(strOrNull(f, 2));
        l.setPhone(strOrNull(f, 3));
        l.setActive(str(f, 4).isEmpty() || bool(f, 4));
        l.setLatitude(number(strOrNull(f, 5)));
        l.setLongitude(number(strOrNull(f, 6)));
        return l;
    }

    private static Double number(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(text.trim());
        } catch (NumberFormatException bad) {
            return null;
        }
    }
}
