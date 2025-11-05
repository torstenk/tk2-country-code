package no.tk2.eessi.cc;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CountryCodeService {

  private static final String ENDPOINT = "https://publications.europa.eu/webapi/rdf/sparql";
  private static final String QUERY_FILE = "ISG-TO-ISO.sparql";

  public List<CountryResult> queryByIsgCouValue(String isgCouValue) throws IOException {
    String sparql = loadQuery();
    // Replace the hardcoded filter with a parameterized variable
    sparql = sparql.replace(
            "FILTER(?isg_cou_value = \"REPLACE_ME\")",
            "FILTER(?isg_cou_value = ?isgCouValue)"
    );

    SPARQLRepository repo = new SPARQLRepository(ENDPOINT);
    try (RepositoryConnection conn = repo.getConnection()) {
      TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
      query.setBinding("isgCouValue", conn.getValueFactory().createLiteral(isgCouValue));
      try (TupleQueryResult result = query.evaluate()) {
        return result.stream()
                .map(bindingSet -> new CountryResult(
                        bindingSet.getValue("isg_cou_value").stringValue(),
                        bindingSet.getValue("iso_3166_1_alpha_2_value").stringValue(),
                        bindingSet.getValue("country_label_en").stringValue(),
                        bindingSet.getValue("country_uri").stringValue()
                ))
                .collect(Collectors.toList());
      }
    }
  }

  public String loadQuery() throws IOException {
    ClassPathResource resource = new ClassPathResource(QUERY_FILE);
    byte[] bytes = resource.getInputStream().readAllBytes();
    return new String(bytes, StandardCharsets.UTF_8);
  }

  public static class CountryResult {
    public final String isgCouValue;
    public final String iso3166Alpha2Value;
    public final String countryLabelEn;
    public final String countryUri;

    public CountryResult(String isgCouValue, String iso3166Alpha2Value, String countryLabelEn, String countryUri) {
      this.isgCouValue = isgCouValue;
      this.iso3166Alpha2Value = iso3166Alpha2Value;
      this.countryLabelEn = countryLabelEn;
      this.countryUri = countryUri;
    }
  }
}