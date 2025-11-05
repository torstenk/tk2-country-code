---
theme: solarized
---
### Semantic Interoperability in EESSI

in

EESSI Information Architecture Expert Group

IAEG meeting 2025-11-05  
(torsten.kirschner@nav.no)
---
How to get lured into semantic interoperability stuff

In EESSI, we've had two member states that used different country codes than their ISO 3166-1 codes. 

The United Kingdom uses UK in EESSI, but GB otherwise.

Greece uses EL in EESSI, but GR otherwise.

What?

---

The EU's publication office makes lots of information available. Look!

[United Kingdom](https://op.europa.eu/en/web/eu-vocabularies/concept/-/resource?uri=http://publications.europa.eu/resource/authority/country/GBR)

[Greece](https://op.europa.eu/en/web/eu-vocabularies/concept/-/resource?uri=http://publications.europa.eu/resource/authority/country/GRC)

So what is this [ISG](https://style-guide.europa.eu/home) ? 

---

Do I have to browse this information manually?

No. They have a query endpoint: https://publications.europa.eu/webapi/rdf/sparql

[Try it out!](https://publications.europa.eu/webapi/rdf/sparql?default-graph-uri=&query=PREFIX+skos%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2004%2F02%2Fskos%2Fcore%23%3E%0D%0APREFIX+euvoc%3A+%3Chttp%3A%2F%2Fpublications.europa.eu%2Fontology%2Feuvoc%23%3E%0D%0A%0D%0ASELECT+DISTINCT%0D%0A++++%3Fcountry_uri%0D%0A++++%28STR%28%3Fcountry_label_en%29+AS+%3Fcountry_label_en%29%0D%0A++++%28STR%28%3Fcountry_label_no%29+AS+%3Fcountry_label_no%29%0D%0A++++%3Fisg_cou_str%0D%0A++++%3Fiso_3166_1_alpha_2_str%0D%0A++++%3Fiso_3166_1_alpha_3_str%0D%0AFROM+%3Chttp%3A%2F%2Fpublications.europa.eu%2Fresource%2Fauthority%2Fcountry%3E%0D%0AWHERE+%7B%0D%0A++++%3Fcountry_uri+a+skos%3AConcept+.%0D%0A++++%3Fcountry_uri+euvoc%3Acontext+%3Chttp%3A%2F%2Fpublications.europa.eu%2Fresource%2Fauthority%2Fuse-context%2FEU_EFTA_UK%3E+.%0D%0A%0D%0A++++%23+English+label%0D%0A++++%3Fcountry_uri+skos%3AprefLabel+%3Fcountry_label_en+.%0D%0A++++FILTER+%28lang%28%3Fcountry_label_en%29+%3D+%22en%22%29%0D%0A%0D%0A++++%23+Norwegian+label%0D%0A++++OPTIONAL+%7B%0D%0A++++++++%3Fcountry_uri+skos%3AprefLabel+%3Fcountry_label_no+.%0D%0A++++++++FILTER+%28lang%28%3Fcountry_label_no%29+%3D+%22no%22%29%0D%0A++++%7D%0D%0A%0D%0A%0D%0A++++%23+Exclude+the+European+Union+%28EUR%29+and+OP_DATPRO+entries%0D%0A++++FILTER+%28%3Fcountry_uri+%21%3D+%3Chttp%3A%2F%2Fpublications.europa.eu%2Fresource%2Fauthority%2Fcountry%2FEUR%3E%29%0D%0A++++FILTER+%28%3Fcountry_uri+%21%3D+%3Chttp%3A%2F%2Fpublications.europa.eu%2Fresource%2Fauthority%2Fcountry%2FOP_DATPRO%3E%29%0D%0A%0D%0A++++OPTIONAL+%7B%0D%0A++++++++%3Fcountry_uri+skos%3Anotation+%3Fisg_cou+.%0D%0A++++++++FILTER%28datatype%28%3Fisg_cou%29+%3D+euvoc%3AISG_COU%29%0D%0A++++++++BIND%28str%28%3Fisg_cou%29+AS+%3Fisg_cou_str%29%0D%0A++++%7D%0D%0A%0D%0A++++OPTIONAL+%7B%0D%0A++++++++%3Fcountry_uri+skos%3Anotation+%3Fiso_3166_1_alpha_2+.%0D%0A++++++++FILTER%28datatype%28%3Fiso_3166_1_alpha_2%29+%3D+euvoc%3AISO_3166_1_ALPHA_2%29%0D%0A++++++++BIND%28str%28%3Fiso_3166_1_alpha_2%29+AS+%3Fiso_3166_1_alpha_2_str%29%0D%0A++++%7D%0D%0A%0D%0A++++OPTIONAL+%7B%0D%0A++++++++%3Fcountry_uri+skos%3Anotation+%3Fiso_3166_1_alpha_3+.%0D%0A++++++++FILTER%28datatype%28%3Fiso_3166_1_alpha_3%29+%3D+euvoc%3AISO_3166_1_ALPHA_3%29%0D%0A++++++++BIND%28str%28%3Fiso_3166_1_alpha_3%29+AS+%3Fiso_3166_1_alpha_3_str%29%0D%0A++++%7D%0D%0A%7D%0D%0ALIMIT+100%0D%0A&format=text%2Fx-html%2Btr&timeout=0&run=+Run+Query+)


---

I don't know RDF or SPARQL.

But after a bit of reading, and lots of AI, I arrived at this query to aid our national implementation with mapping between EESSI, i.e., ISC codes to ISO 3166-1 α-1 and α-3 codes.

---

```
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  
PREFIX euvoc: <http://publications.europa.eu/ontology/euvoc#>  
  
SELECT DISTINCT  
    ?isg_cou_value  
    ?iso_3166_1_alpha_2_value    ?country_label_en    ?country_uriWHERE {  
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
        FILTER(?isg_cou_value = "REPLACE_ME")  # Replace with actual ISG COU value  
  
        # Get ISO 3166-1 alpha-2 value        ?country_uri skos:notation ?iso_3166_1_alpha_2 .  
        FILTER(datatype(?iso_3166_1_alpha_2) = euvoc:ISO_3166_1_ALPHA_2)  
        BIND(str(?iso_3166_1_alpha_2) AS ?iso_3166_1_alpha_2_value)  
  
        # Exclude the European Union (EUR) and OP_DATPRO entries  
        FILTER (?country_uri != <http://publications.europa.eu/resource/authority/country/EUR>)  
        FILTER (?country_uri != <http://publications.europa.eu/resource/authority/country/OP_DATPRO>)  
    }  
}
```


---

And using RDF4J, it is possible to use SPARQL or just [Java](https://github.com/torstenk/tk2-country-code) to query.

(DEMO)
---
Do you think this could be useful to explore further?

Suspicions: SDG uses this stuff for the mapping of domains and possibly structured documents for evidences to be exchanged.
---
Thank You!
