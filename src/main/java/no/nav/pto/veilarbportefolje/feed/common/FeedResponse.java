package no.nav.pto.veilarbportefolje.feed.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class FeedResponse {
    String nextPageId;
    List<FeedElement> elements;
}
