package no.nav.pto.veilarbportefolje.oppfolging;

import no.nav.common.types.identer.AktorId;
import no.nav.pto.veilarbportefolje.database.Table;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static no.nav.pto.veilarbportefolje.oppfolging.OppfolgingRepository.safeToJaNei;
import static no.nav.pto.veilarbportefolje.util.TestUtil.setupInMemoryDatabase;
import static org.assertj.core.api.Assertions.assertThat;

public class OppfolgingRepositoryTest {

    private JdbcTemplate db;
    private OppfolgingRepository oppfolgingRepository;

    @Before
    public void setup() {
        DataSource ds = setupInMemoryDatabase();
        db = new JdbcTemplate(ds);
        oppfolgingRepository = new OppfolgingRepository(db);
    }

    @Test
    public void skal_sette_oppfolging_til_false() {
        final AktorId aktoerId = AktorId.of("0");

        SqlUtils.insert(db, Table.OPPFOLGING_DATA.TABLE_NAME)
                .value(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString())
                .value(Table.OPPFOLGING_DATA.OPPFOLGING, "J")
                .execute();

        oppfolgingRepository.settOppfolgingTilFalse(aktoerId);

        final String oppfolging = SqlUtils.select(db, Table.OPPFOLGING_DATA.TABLE_NAME, rs -> rs.getString(Table.OPPFOLGING_DATA.OPPFOLGING))
                .column(Table.OPPFOLGING_DATA.OPPFOLGING)
                .where(WhereClause.equals(Table.OPPFOLGING_DATA.AKTOERID, aktoerId.toString()))
                .execute();

        assertThat(oppfolging).isEqualTo("N");
    }

    @Test
    public void skal_returnere_ja_nei_streng() {
        assertThat(safeToJaNei(true)).isEqualTo("J");
        assertThat(safeToJaNei(false)).isEqualTo("N");
        assertThat(safeToJaNei(null)).isEqualTo("N");
    }

}
