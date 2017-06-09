package no.nav.fo.domene.Aktivitet;

public enum AktivitetFullfortStatuser {
    fullfort,
    avbrutt,
    gjennomført;

    public static boolean contains(String value) {
        try {
            AktivitetFullfortStatuser.valueOf(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
