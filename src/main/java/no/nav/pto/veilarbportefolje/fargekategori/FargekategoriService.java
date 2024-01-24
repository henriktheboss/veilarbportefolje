package no.nav.pto.veilarbportefolje.fargekategori;

import lombok.RequiredArgsConstructor;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.pto.veilarbportefolje.fargekategori.FargekategoriController.OppdaterFargekategoriRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FargekategoriService {

    private final FargekategoriRepository fargekategoriRepository;

    public UUID oppdaterFargekategoriForBruker(OppdaterFargekategoriRequest request, VeilederId sistEndretAv) {
        return fargekategoriRepository.upsertFargekateori(request, sistEndretAv);
    }
}
