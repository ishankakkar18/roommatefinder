package in.flatmate.reference;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Read-only lookups for cities and areas. */
@Repository
public class ReferenceRepository {

    public record CityRef(Integer id, String name, String slug) {
    }

    public record AreaRef(Integer id, String name, String slug, Integer cityId) {
    }

    private final JdbcTemplate jdbc;

    public ReferenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CityRef> cities() {
        return jdbc.query("select id, name, slug from cities order by name",
                (rs, i) -> new CityRef(rs.getInt("id"), rs.getString("name"), rs.getString("slug")));
    }

    public List<AreaRef> areas(Integer cityId) {
        return jdbc.query("select id, name, slug, city_id from areas where city_id = ? order by name",
                (rs, i) -> new AreaRef(rs.getInt("id"), rs.getString("name"),
                        rs.getString("slug"), rs.getInt("city_id")),
                cityId);
    }

    public Optional<String> cityName(Integer cityId) {
        return jdbc.query("select name from cities where id = ?",
                (rs, i) -> rs.getString(1), cityId).stream().findFirst();
    }

    public Optional<String> areaName(Integer areaId) {
        return jdbc.query("select name from areas where id = ?",
                (rs, i) -> rs.getString(1), areaId).stream().findFirst();
    }

    public boolean areaBelongsToCity(Integer areaId, Integer cityId) {
        Integer n = jdbc.queryForObject(
                "select count(*) from areas where id = ? and city_id = ?", Integer.class, areaId, cityId);
        return n != null && n > 0;
    }

    /** True when the two areas are the same or directly adjacent. */
    public boolean sameOrAdjacent(Integer a, Integer b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.equals(b)) {
            return true;
        }
        Integer n = jdbc.queryForObject("""
                select count(*) from area_adjacency
                where (area_a_id = ? and area_b_id = ?) or (area_a_id = ? and area_b_id = ?)
                """, Integer.class, a, b, b, a);
        return n != null && n > 0;
    }
}
