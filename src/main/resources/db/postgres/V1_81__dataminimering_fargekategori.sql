-- Slett alle rader fra fargekategori-tabell for brukere som ikke er under oppfølging
DELETE FROM fargekategori WHERE fnr IN (
    SELECT f.fnr FROM aktive_identer ai
    RIGHT JOIN fargekategori f
    ON ai.fnr = f.fnr
    WHERE ai.fnr IS NULL
);