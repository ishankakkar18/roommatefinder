-- ============================================================================
-- V2__seed_cities_areas.sql
-- Minimal reference data: one city, eight areas, adjacency pairs.
-- Adjacency is symmetric data but stored once, canonical (a_id < b_id).
-- ============================================================================

SET NAMES utf8mb4;

INSERT INTO cities (name, slug) VALUES ('Bengaluru', 'bengaluru');

INSERT INTO areas (city_id, name, slug)
SELECT c.id, a.name, a.slug
FROM cities c
JOIN (
    SELECT 'Koramangala' AS name, 'koramangala' AS slug UNION ALL
    SELECT 'HSR Layout',   'hsr-layout'           UNION ALL
    SELECT 'Indiranagar',  'indiranagar'          UNION ALL
    SELECT 'Domlur',       'domlur'               UNION ALL
    SELECT 'Whitefield',   'whitefield'           UNION ALL
    SELECT 'Marathahalli', 'marathahalli'         UNION ALL
    SELECT 'Bellandur',    'bellandur'            UNION ALL
    SELECT 'BTM Layout',   'btm-layout'
) a
WHERE c.slug = 'bengaluru';

-- Adjacent neighbourhoods, one row per pair, smaller id first.
-- x.id < y.id keeps only ONE direction per pair, otherwise the pair matches
-- twice (a->b and b->a) and violates the primary key.
INSERT INTO area_adjacency (area_a_id, area_b_id)
SELECT x.id, y.id
FROM areas x
JOIN areas y ON x.city_id = y.city_id AND x.id < y.id
JOIN (
    SELECT 'koramangala' AS a, 'hsr-layout'  AS b UNION ALL
    SELECT 'koramangala',      'indiranagar'     UNION ALL
    SELECT 'koramangala',      'domlur'          UNION ALL
    SELECT 'koramangala',      'btm-layout'      UNION ALL
    SELECT 'hsr-layout',       'btm-layout'      UNION ALL
    SELECT 'hsr-layout',       'bellandur'       UNION ALL
    SELECT 'indiranagar',      'domlur'          UNION ALL
    SELECT 'whitefield',       'marathahalli'    UNION ALL
    SELECT 'whitefield',       'bellandur'       UNION ALL
    SELECT 'marathahalli',     'bellandur'
) p ON (x.slug = p.a AND y.slug = p.b) OR (x.slug = p.b AND y.slug = p.a);
