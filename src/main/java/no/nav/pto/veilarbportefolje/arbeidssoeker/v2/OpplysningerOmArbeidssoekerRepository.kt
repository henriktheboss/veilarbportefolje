package no.nav.pto.veilarbportefolje.arbeidssoeker.v2

import no.nav.pto.veilarbportefolje.database.PostgresTable.OPPLYSNINGER_OM_ARBEIDSSOEKER.*
import no.nav.pto.veilarbportefolje.database.PostgresTable.OPPLYSNINGER_OM_ARBEIDSSOEKER_JOBBSITUASJON
import no.nav.pto.veilarbportefolje.postgres.PostgresUtils
import no.nav.pto.veilarbportefolje.util.DateUtils
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Types
import java.util.*

@Repository
class OpplysningerOmArbeidssoekerRepository(
    private val db: JdbcTemplate
) {
    fun insertOpplysningerOmArbeidssoekerOgJobbsituasjon(opplysningerOmArbeidssoeker: OpplysningerOmArbeidssoeker) {
        insertOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoeker)
        insertOpplysningerOmArbeidssoekerJobbsituasjon(opplysningerOmArbeidssoeker.opplysningerOmJobbsituasjon)
    }

    private fun insertOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoeker: OpplysningerOmArbeidssoeker) {
        val sqlString = """INSERT INTO $TABLE_NAME ( 
                    $OPPLYSNINGER_OM_ARBEIDSSOEKER_ID, 
                    $PERIODE_ID,
                    $SENDT_INN_TIDSPUNKT,
                    $UTDANNING_NUS_KODE,
                    $UTDANNING_BESTATT,
                    $UTDANNING_GODKJENT
                )
                VALUES (?, ?, ?, ?, ?, ?)"""
        db.update(
            sqlString,
            opplysningerOmArbeidssoeker.opplysningerOmArbeidssoekerId,
            opplysningerOmArbeidssoeker.periodeId,
            DateUtils.toTimestamp(opplysningerOmArbeidssoeker.sendtInnTidspunkt),
            opplysningerOmArbeidssoeker.utdanningNusKode,
            opplysningerOmArbeidssoeker.utdanningBestatt,
            opplysningerOmArbeidssoeker.utdanningGodkjent
        )
    }

    private fun insertOpplysningerOmArbeidssoekerJobbsituasjon(opplysningerOmArbeidssoekerJobbsituasjon: OpplysningerOmArbeidssoekerJobbsituasjon) {
        val sqlString = """INSERT INTO ${OPPLYSNINGER_OM_ARBEIDSSOEKER_JOBBSITUASJON.TABLE_NAME} ( 
                    ${OPPLYSNINGER_OM_ARBEIDSSOEKER_JOBBSITUASJON.OPPLYSNINGER_OM_ARBEIDSSOEKER_ID}, 
                    ${OPPLYSNINGER_OM_ARBEIDSSOEKER_JOBBSITUASJON.JOBBSITUASJON}
                )
                VALUES (?, ?)"""

        db.batchUpdate(sqlString, object : BatchPreparedStatementSetter {
            @Throws(SQLException::class)
            override fun setValues(ps: PreparedStatement, i: Int) {
                ps.setObject(1, opplysningerOmArbeidssoekerJobbsituasjon.opplysningerOmArbeidssoekerId, Types.OTHER)
                ps.setString(2, opplysningerOmArbeidssoekerJobbsituasjon.jobbsituasjon[i].name)
            }

            override fun getBatchSize(): Int {
                return opplysningerOmArbeidssoekerJobbsituasjon.jobbsituasjon.size
            }
        })
    }

    fun harSisteOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoekerId: UUID): Boolean {
        val sqlString = """SELECT COUNT(*) FROM $TABLE_NAME WHERE $OPPLYSNINGER_OM_ARBEIDSSOEKER_ID = ?"""
        val count: Int = PostgresUtils.queryForObjectOrNull {
            db.queryForObject(sqlString, Int::class.java, opplysningerOmArbeidssoekerId)
        } ?: 0

        return count != 0
    }

    fun slettOpplysningerOmArbeidssoeker(arbeidssoekerPeriodeId: UUID?): Int {
        val sqlString = """DELETE FROM $TABLE_NAME WHERE $PERIODE_ID = ?"""
        return db.update(sqlString, arbeidssoekerPeriodeId)
    }
}