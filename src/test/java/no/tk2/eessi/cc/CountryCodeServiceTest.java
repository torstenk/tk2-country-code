package no.tk2.eessi.cc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CountryCodeServiceTest {

  @Test
  void queryByIsgCouValue_withNO_returnsResults() throws Exception {
    CountryCodeService service = new CountryCodeService();
    List<CountryCodeService.CountryResult> results = service.queryByIsgCouValue("NO");
    assertNotNull(results);
    assertFalse(results.isEmpty(), "Expected at least one result for ISG country code 'NO'");
    log.info("results.size={}", results.size());
    CountryCodeService.CountryResult country = results.getFirst();
    assertEquals("NO", country.isgCouValue);
    log.info("Country Result: ISG={}, ISO2={}, Label={}, URI={}", country.isgCouValue, country.iso3166Alpha2Value, country.countryLabelEn, country.countryUri);
    assertNotNull(country.iso3166Alpha2Value);
    assertNotNull(country.countryLabelEn);
    assertNotNull(country.countryUri);
  }

  @Test
  void loadQuery() throws Exception {
    CountryCodeService service = new CountryCodeService();
    String query = service.loadQuery();
    assertNotNull(query);
    assertFalse(query.isBlank());
    assertTrue(query.contains("SELECT"), "SPARQL query should contain SELECT");
  }
}