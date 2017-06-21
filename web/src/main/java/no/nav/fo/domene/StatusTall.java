package no.nav.fo.domene;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatusTall {
    private long totalt;
    private long nyeBrukere;
    private long inaktiveBrukere;
    private long venterPaSvarFraNAV;
    private long venterPaSvarFraBruker;
    private long iavtaltAktivitet;
    private long ikkeIavtaltAktivitet;
    private long utlopteAktiviteter;
}