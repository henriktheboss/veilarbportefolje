package no.nav.pto.veilarbportefolje.arbeidssoker

import no.nav.pto.veilarbportefolje.database.PostgresTable.SISTE_ARBEIDSSOKER_PERIODE
import org.springframework.jdbc.core.JdbcTemplate

class ArbeidssokerPeriodeRepository(
    val db: JdbcTemplate
) {
    fun upsert(sistePeriode: SistePeriode) {
        val sqlString = """INSERT INTO ${SISTE_ARBEIDSSOKER_PERIODE.TABLE_NAME} ( 
                    ${SISTE_ARBEIDSSOKER_PERIODE.FNR}, 
                    ${SISTE_ARBEIDSSOKER_PERIODE.ARBEIDSSOKER_PERIODE_ID}
                )
                VALUES (?, ?)
                ON CONFLICT (${SISTE_ARBEIDSSOKER_PERIODE.FNR})
                DO UPDATE SET ${SISTE_ARBEIDSSOKER_PERIODE.ARBEIDSSOKER_PERIODE_ID} = ?"""
        db.update(sqlString, sistePeriode.fnr.get(), sistePeriode.id, sistePeriode.id)
    }

}
