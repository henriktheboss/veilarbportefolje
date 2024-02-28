-- Legger til ny kolonne enhet_id for brukers enhet
ALTER TABLE fargekategori
ADD COLUMN IF NOT EXISTS enhet_id VARCHAR(4) DEFAULT NULL;

-- Setter enhet_id basert på brukers oppfølgingsenhet
UPDATE fargekategori AS f
SET enhet_id = ob.nav_kontor
FROM oppfolgingsbruker_arena_v2 ob
WHERE f.fnr = ob.fodselsnr
AND f.enhet_id is null;

-- Legger på NOT NULL constraint på enhet_id
-- Dette må gjøres til slutt siden vi er nødt
-- å ha en default verdi når kolonnen opprettes
ALTER TABLE fargekategori
ALTER COLUMN enhet_id
SET NOT NULL;