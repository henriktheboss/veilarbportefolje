package no.nav.pto.veilarbportefolje.profilering

import no.nav.arbeid.soker.profilering.ArbeidssokerProfilertEvent
import no.nav.pto.veilarbportefolje.database.PostgresTable.BRUKER_PROFILERING_V2
import no.nav.pto.veilarbportefolje.util.DateUtils
import org.springframework.jdbc.core.JdbcTemplate

class BrukerProfileringRepositoryV3(
    private val db: JdbcTemplate
) {
    fun upsertBrukerProfilering(kafkaMelding: ArbeidssokerProfilertEvent) {
        val sqlString = """INSERT INTO ${BRUKER_PROFILERING_V2.TABLE_NAME} ( 
                    ${BRUKER_PROFILERING_V2.FNR}, 
                    ${BRUKER_PROFILERING_V2.PERIODEID},
                    ${BRUKER_PROFILERING_V2.PROFILERING_RESULTAT},
                    ${BRUKER_PROFILERING_V2.PROFILERING_TIDSPUNKT} 
                )
                VALUES (?, ?, ?, ?)
                ON CONFLICT (${BRUKER_PROFILERING_V2.FNR}, ${BRUKER_PROFILERING_V2.PERIODEID})
                DO UPDATE SET (${BRUKER_PROFILERING_V2.PROFILERING_RESULTAT},${BRUKER_PROFILERING_V2.PROFILERING_TIDSPUNKT}) = (?, ?)"""
        db.update(
            sqlString,
            kafkaMelding.aktorid,
            kafkaMelding.profilertTil.name,
            DateUtils.zonedDateStringToTimestamp(kafkaMelding.profileringGjennomfort),
            kafkaMelding.profilertTil.name,
            DateUtils.zonedDateStringToTimestamp(kafkaMelding.profileringGjennomfort)
        )
    }
}