package no.nav.fo.util;

import no.nav.fo.domene.Bruker;
import no.nav.fo.domene.Portefolje;
import no.nav.fo.service.PepClient;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class PortefoljeUtils {

    public static Portefolje buildPortefolje(List<Bruker> brukere, List<Bruker> brukereSublist, String enhet, int fra) {

        return new Portefolje()
                .setEnhet(enhet)
                .setBrukere(brukereSublist)
                .setAntallTotalt(brukere.size())
                .setAntallReturnert(brukereSublist.size())
                .setFraIndex(fra);
    }

    public static List<Bruker> getSublist(List<Bruker> brukere, int fra, int antall) {
        return brukere.stream().skip(fra).limit(antall).collect(toList());
    }

    public static List<Bruker> filtrerBrukere(List<Bruker> brukere, String veilederIdent, PepClient pepClient) {
        return brukere.stream()
                .map( bruker -> fjernNavnOgFnrDersomIkkeTilgang(bruker, pepClient.isServiceCallAllowed(bruker.getFnr(),veilederIdent)))
                .collect(toList());
    }

    private static Bruker fjernNavnOgFnrDersomIkkeTilgang(Bruker bruker, Boolean skalNavnOgFnrVises) {
        return skalNavnOgFnrVises ? bruker : fjernNavnOgFnr(bruker);
    }

    private static Bruker fjernNavnOgFnr(Bruker bruker) {
        return bruker.setFnr("").setEtternavn("").setFornavn("").setKjonn("").setFodselsdato("");
    }
}
