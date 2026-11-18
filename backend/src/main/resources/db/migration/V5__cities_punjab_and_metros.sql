-- ============================================================================
-- V5__cities_punjab_and_metros.sql
-- Replaces the single Bengaluru seed with Punjab + major Indian cities.
--
-- Destructive on purpose: area_id is a foreign key, so the old reference data
-- cannot be removed while profiles point at it. All match/chat data and
-- profiles are reset; user accounts are kept.
-- ============================================================================

DELETE FROM read_cursors;
DELETE FROM messages;
DELETE FROM conversations;
DELETE FROM match_requests;
DELETE FROM profiles;
DELETE FROM area_adjacency;
DELETE FROM areas;
DELETE FROM cities;

INSERT INTO cities (name, slug) VALUES
    ('Chandigarh', 'chandigarh'),
    ('Ludhiana',   'ludhiana'),
    ('Amritsar',   'amritsar'),
    ('Jalandhar',  'jalandhar'),
    ('Delhi',      'delhi'),
    ('Mumbai',     'mumbai'),
    ('Pune',       'pune'),
    ('Hyderabad',  'hyderabad');

INSERT INTO areas (city_id, name, slug)
SELECT c.id, a.name, a.slug
FROM cities c
JOIN (
    SELECT 'chandigarh' AS city, 'Sector 17'       AS name, 'sector-17'       AS slug UNION ALL
    SELECT 'chandigarh',        'Sector 22',              'sector-22'             UNION ALL
    SELECT 'chandigarh',        'Sector 35',              'sector-35'             UNION ALL
    SELECT 'chandigarh',        'Sector 43',              'sector-43'             UNION ALL
    SELECT 'chandigarh',        'Sector 15',              'sector-15'             UNION ALL
    SELECT 'chandigarh',        'Mohali',                 'mohali'                UNION ALL
    SELECT 'chandigarh',        'Panchkula',              'panchkula'             UNION ALL
    SELECT 'chandigarh',        'Zirakpur',               'zirakpur'              UNION ALL

    SELECT 'ludhiana',          'Sarabha Nagar',          'sarabha-nagar'         UNION ALL
    SELECT 'ludhiana',          'Model Town',             'model-town-ludhiana'   UNION ALL
    SELECT 'ludhiana',          'Civil Lines',            'civil-lines-ludhiana'  UNION ALL
    SELECT 'ludhiana',          'Dugri',                  'dugri'                 UNION ALL
    SELECT 'ludhiana',          'Pakhowal Road',          'pakhowal-road'         UNION ALL
    SELECT 'ludhiana',          'BRS Nagar',              'brs-nagar'             UNION ALL

    SELECT 'amritsar',          'Ranjit Avenue',          'ranjit-avenue'         UNION ALL
    SELECT 'amritsar',          'Lawrence Road',          'lawrence-road'         UNION ALL
    SELECT 'amritsar',          'Green Avenue',           'green-avenue'          UNION ALL
    SELECT 'amritsar',          'Majitha Road',           'majitha-road'          UNION ALL
    SELECT 'amritsar',          'Court Road',             'court-road'            UNION ALL

    SELECT 'jalandhar',         'Model Town',             'model-town-jalandhar'  UNION ALL
    SELECT 'jalandhar',         'Urban Estate',           'urban-estate'          UNION ALL
    SELECT 'jalandhar',         'Defence Colony',         'defence-colony'        UNION ALL
    SELECT 'jalandhar',         'Kapurthala Road',        'kapurthala-road'       UNION ALL
    SELECT 'jalandhar',         'Jalandhar Cantt',        'jalandhar-cantt'       UNION ALL

    SELECT 'delhi',             'Saket',                  'saket'                 UNION ALL
    SELECT 'delhi',             'Hauz Khas',              'hauz-khas'             UNION ALL
    SELECT 'delhi',             'Lajpat Nagar',           'lajpat-nagar'          UNION ALL
    SELECT 'delhi',             'Dwarka',                 'dwarka'                UNION ALL
    SELECT 'delhi',             'Rohini',                 'rohini'                UNION ALL
    SELECT 'delhi',             'Karol Bagh',             'karol-bagh'            UNION ALL

    SELECT 'mumbai',            'Andheri',                'andheri'               UNION ALL
    SELECT 'mumbai',            'Bandra',                 'bandra'                UNION ALL
    SELECT 'mumbai',            'Powai',                  'powai'                 UNION ALL
    SELECT 'mumbai',            'Thane',                  'thane'                 UNION ALL
    SELECT 'mumbai',            'Malad',                  'malad'                 UNION ALL
    SELECT 'mumbai',            'Colaba',                 'colaba'                UNION ALL

    SELECT 'pune',              'Kothrud',                'kothrud'               UNION ALL
    SELECT 'pune',              'Baner',                  'baner'                 UNION ALL
    SELECT 'pune',              'Hinjewadi',              'hinjewadi'             UNION ALL
    SELECT 'pune',              'Viman Nagar',            'viman-nagar'           UNION ALL
    SELECT 'pune',              'Kalyani Nagar',          'kalyani-nagar'         UNION ALL

    SELECT 'hyderabad',         'Gachibowli',             'gachibowli'            UNION ALL
    SELECT 'hyderabad',         'Madhapur',               'madhapur'              UNION ALL
    SELECT 'hyderabad',         'Kukatpally',             'kukatpally'            UNION ALL
    SELECT 'hyderabad',         'Banjara Hills',          'banjara-hills'
) a ON a.city = c.slug;

-- Chain each city's areas in order, so "same or adjacent" is meaningful
-- without hand-writing every pair.
INSERT INTO area_adjacency (area_a_id, area_b_id)
SELECT LEAST(t.id, t.prev_id), GREATEST(t.id, t.prev_id)
FROM (
    SELECT id, LAG(id) OVER (PARTITION BY city_id ORDER BY name) AS prev_id
    FROM areas
) t
WHERE t.prev_id IS NOT NULL;
