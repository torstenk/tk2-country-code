package no.tk2.eessi.cc;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class AppTests {

  @Test
  void contextLoads() {
  }

  @Test
  void testQueryByIsgCouValue() throws IOException {
    // Arrange
    String sparqlQuery = """
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            PREFIX euvoc: <http://publications.europa.eu/ontology/euvoc#>
            
            SELECT DISTINCT
                ?isg_cou_value
                ?iso_3166_1_alpha_2_value
                ?country_label_en
                ?country_uri
            WHERE {
                # Query the EU countries dataset
                GRAPH <http://publications.europa.eu/resource/authority/country> {
                    ?country_uri a skos:Concept .
                    ?country_uri euvoc:context <http://publications.europa.eu/resource/authority/use-context/EU_EFTA_UK> .
            
                    # Get English label
                    ?country_uri skos:prefLabel ?country_label_en .
                    FILTER (lang(?country_label_en) = "en")
            
                    # Get ISG COU value and filter for specific value
                    ?country_uri skos:notation ?isg_cou .
                    FILTER(datatype(?isg_cou) = euvoc:ISG_COU)
                    BIND(str(?isg_cou) AS ?isg_cou_value)
                    FILTER(?isg_cou_value = "UK")  # Replace with actual ISG COU value
            
                    # Get ISO 3166-1 alpha-2 value
                    ?country_uri skos:notation ?iso_3166_1_alpha_2 .
                    FILTER(datatype(?iso_3166_1_alpha_2) = euvoc:ISO_3166_1_ALPHA_2)
                    BIND(str(?iso_3166_1_alpha_2) AS ?iso_3166_1_alpha_2_value)
            
                    # Exclude the European Union (EUR) and OP_DATPRO entries
                    FILTER (?country_uri != <http://publications.europa.eu/resource/authority/country/EUR>)
                    FILTER (?country_uri != <http://publications.europa.eu/resource/authority/country/OP_DATPRO>)
                }
            }
            """;

    CountryCodeService service = Mockito.spy(new CountryCodeService());

    // Mock query loading
    doReturn(sparqlQuery).when(service).loadQuery();

    // Mock RDF4J objects
    SPARQLRepository repo = mock(SPARQLRepository.class);
    RepositoryConnection conn = mock(RepositoryConnection.class);
    TupleQueryResult result = mock(TupleQueryResult.class);
    BindingSet bindingSet = mock(BindingSet.class);

    // Mock value factory
    ValueFactory vf = SimpleValueFactory.getInstance();

    // Mock repository connection
    when(repo.getConnection()).thenReturn(conn);
    when(conn.prepareTupleQuery(any(), any())).thenReturn(mock(org.eclipse.rdf4j.query.TupleQuery.class));
    when(conn.getValueFactory()).thenReturn(vf);

    // Mock result
    when(result.stream()).thenReturn(Collections.singletonList(bindingSet).stream());
    when(bindingSet.getValue("isg_cou_value")).thenReturn(vf.createLiteral("UK"));
    when(bindingSet.getValue("iso_3166_1_alpha_2_value")).thenReturn(vf.createLiteral("GB"));
    when(bindingSet.getValue("country_label_en")).thenReturn(vf.createLiteral("United Kingdom"));
    when(bindingSet.getValue("country_uri")).thenReturn(vf.createIRI("http://publications.europa.eu/resource/authority/country/GBR"));

    // Act
    List<CountryCodeService.CountryResult> results = service.queryByIsgCouValue("UK");

    // Assert
    assertEquals(1, results.size());
    CountryCodeService.CountryResult country = results.getFirst();
    assertEquals("UK", country.isgCouValue);
    assertEquals("GB", country.iso3166Alpha2Value);
    assertEquals("United Kingdom", country.countryLabelEn);
    assertEquals("http://publications.europa.eu/resource/authority/country/GBR", country.countryUri);
  }

}
