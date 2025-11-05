
https://publications.europa.eu/webapi/rdf/sparql


String query = "SELECT * WHERE {?X ?P ?Y }";
TupleQuery preparedQuery = conn.prepareQuery(QuerLanguage.SPARQL, query);
preparedQuery.setBinding("X", someValue);
...
TupleQueryResult result = preparedQuery.evaluate();
