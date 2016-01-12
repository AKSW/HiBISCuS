package org.aksw.simba.hibiscus.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.hibiscus.HibiscusConfig;
import org.aksw.simba.hibiscus.HibiscusSourceSelection;
import org.aksw.sparql.query.algebra.helpers.BGPGroupGenerator;
import org.openrdf.query.algebra.StatementPattern;

import com.fluidops.fedx.FedX;
import com.fluidops.fedx.FederationManager;
import com.fluidops.fedx.algebra.StatementSource;
import com.fluidops.fedx.cache.Cache;
import com.fluidops.fedx.structures.Endpoint;
/**
 * Test Source Selection
 * @author Saleem
 *
 */
public class TestSourceSelection {
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		long strtTime = System.currentTimeMillis();
		String FedSummaries = "summaries\\BigRDFBench-HiBISCus.n3";
		
		String mode = "Index_dominant";  //{ASK_dominant, Index_dominant}
		double commonPredThreshold = 0.33 ;  //considered a predicate as common predicate if it is presenet in 33% available data sources
	
		HibiscusConfig.initialize(FedSummaries,mode,commonPredThreshold);  // must call this function only one time at the start to load configuration information. Please specify the FedSum mode. 
		System.out.println("One time configuration loading time : "+ (System.currentTimeMillis()-strtTime));
		List<String> queries = new ArrayList<String>();
		 String C4 = "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
				 + "prefix geonames: <http://www.geonames.org/ontology#>\n"
		+ "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
		+ "Prefix dbpedia: <http://dbpedia.org/ontology/>\n"
		+ "select distinct ?countryName ?countryCode ?locationMap ?population ?longitude ?latitude ?nationalAnthem ?foundingDate ?largestCity ?ethnicGroup ?motto\n"
		+ "{\n"
		+ "?NYTplace geonames:name ?countryName;\n"
		+ "geonames:countryCode ?countryCode;\n"
		+ "geonames:population  ?population;\n"
		+ "geo:long   ?longitude;\n"
		 + "geo:lat     ?latitude;\n"
		+ "owl:sameAs   ?geonameplace.\n"
		+ "optional\n"
		+ "{\n"
		+ "?geonameplace dbpedia:capital ?capital;\n"
		 + "dbpedia:anthem ?nationalAnthem;\n"
		 + "dbpedia:foundingDate ?foundingDate;\n"
		 + "dbpedia:largestCity ?largestCity;\n"
		 + "dbpedia:ethnicGroup ?ethnicGroup;\n"
		 + "dbpedia:motto ?motto.\n"
		 + "}\n"
		 + "} limit 50";
		 
		String SSQ1 = "PREFIX  cp: <http://common/schema/>"
				+ "SELECT * "
				+ " WHERE "
				+ "{ "
				+ "?s   cp:p1      ?v1 . "
				+ "?s   cp:p2      ?v2 . "
				+ "} ";
		//queries.add(SSQ1);
		String SSQ2 = "PREFIX  cp: <http://common/schema/>"
				+ "SELECT * "
				+ " WHERE "
				+ "{ "
				+ "{?s   cp:p1      ?v1 . "
				+ "?s   cp:p2      ?v2 . }"
				+ "UNION"
				+ "{?s cp:p4 cp:o13. "
				+ "?s cp:p5 ?v3.} "
				+ "} ";
		//queries.add(SSQ2);
		String PSQ3 = "PREFIX cp: <http://common/schema/>"
				+ "SELECT *  "
				+ "WHERE "
				+ "{ "
				+ "?s cp:p1 ?v1 . "
				+ "?v1 cp:p3 ?v2 . "
				+ "} ";
		//queries.add(PSQ3);
		String PSQ4 = "PREFIX  cp: <http://common/schema/>"
				+ "PREFIX  ns1_3: <http://auth13/schema/>"
				+ "SELECT * "
				+ "WHERE "
				+ "{"
				+ "ns1_3:s1   ?p       ?v1 . "
				+ "?p           cp:p6     ?v2 . "
				+ "} ";
		//queries.add(PSQ4);
		String HSQ6 = "PREFIX  cp: <http://common/schema/>"
				+ "PREFIX  ns3:<http://auth3/schema/>"
				+ "SELECT * WHERE "
				+ "{"
				+ "ns3:s3  cp:p9    ?v0."
				+ "?s1        cp:p0    ?v0."
				+ "?s1        cp:p1    ?v1 . "
				+ "?v1        cp:p2    ?v2 . "
				+ "?v1     cp:p3   \"o35\"."
				+ "} ";
		//-------------------------------------------------------------------
		String cd1 = "SELECT ?predicate ?object WHERE { " +     //cd1
				"{ <http://dbpedia.org/resource/Barack_Obama> ?predicate ?object }" +
				" UNION " +
				" { ?subject <http://www.w3.org/2002/07/owl#sameAs> <http://dbpedia.org/resource/Barack_Obama> ." +
				"?subject ?predicate ?object }  " +
				"}";
		queries.add(cd1);
		String cd2 = "SELECT ?party ?page  WHERE { " +   //cd2
			  " <http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/ontology/party> ?party ." +
			 " ?x <http://data.nytimes.com/elements/topicPage> ?page ." +
				"   ?x <http://www.w3.org/2002/07/owl#sameAs> <http://dbpedia.org/resource/Barack_Obama> ."+
				"}";
		queries.add(cd2);
		String cd3 = "SELECT ?president ?party ?page WHERE { " + //cd3
	   "?president <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/President> ." +
	   "?president <http://dbpedia.org/ontology/nationality> <http://dbpedia.org/resource/United_States> ." +
	   "?president <http://dbpedia.org/ontology/party> ?party ." +
	   "?x <http://data.nytimes.com/elements/topicPage> ?page ." +
	   "?x <http://www.w3.org/2002/07/owl#sameAs> ?president ." +
	"}";
		queries.add(cd3);
		String cd4 = "SELECT ?actor ?news WHERE {"+   //cd4
			   "?film <http://purl.org/dc/terms/title> 'Tarzan' ."+
				"   ?film <http://data.linkedmdb.org/resource/movie/actor> ?actor ."+
				 "  ?actor <http://www.w3.org/2002/07/owl#sameAs> ?x."+
				  " ?y <http://www.w3.org/2002/07/owl#sameAs> ?x ."+
				  " ?y <http://data.nytimes.com/elements/topicPage> ?news"+
				"}";
		queries.add(cd4);
		String cd5 = "SELECT ?film ?director ?genre WHERE {"+    //cd5 
			   "?film <http://dbpedia.org/ontology/director>  ?director ."+
				"   ?director <http://dbpedia.org/ontology/nationality> <http://dbpedia.org/resource/Italy> ."+
				 "  ?x <http://www.w3.org/2002/07/owl#sameAs> ?film ."+
				  " ?x <http://data.linkedmdb.org/resource/movie/genre> ?genre ."+
				"}";
		queries.add(cd5);
		String cd6 = "SELECT ?name ?location ?news WHERE {"+ //cd 6
			   "?artist <http://xmlns.com/foaf/0.1/name> ?name ."+
				"   ?artist <http://xmlns.com/foaf/0.1/based_near> ?location ."+
				 "  ?location <http://www.geonames.org/ontology#parentFeature> ?germany . "+
				  " ?germany <http://www.geonames.org/ontology#name> 'Federal Republic of Germany'"+
				"}";
		queries.add(cd6);
		String cd7= "SELECT ?location ?news WHERE {"+ //7
			   "?location <http://www.geonames.org/ontology#parentFeature> ?parent ."+ 
				"   ?parent <http://www.geonames.org/ontology#name> 'California' ."+
				 "  ?y <http://www.w3.org/2002/07/owl#sameAs> ?location ."+
				  " ?y <http://data.nytimes.com/elements/topicPage> ?news "+
				"}";
		queries.add(cd7);
		//-----------------------------------------LS-----------------------------------
		String ls1 = "SELECT ?drug ?melt WHERE {"+
		    "{ ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/meltingPoint> ?melt. }"+
		    "    UNION"+
		    "    { ?drug <http://dbpedia.org/ontology/Drug/meltingPoint> ?melt . }"+
		    "}";
		queries.add(ls1);
		String ls2 = "SELECT ?predicate ?object WHERE {"+
		    "{ <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }"+
		    "UNION    "+
		    "{ <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.w3.org/2002/07/owl#sameAs> ?caff ."+
		    "  ?caff ?predicate ?object . } "+
		"}";
		queries.add(ls2);
		String ls3 = "SELECT ?Drug ?IntDrug ?IntEffect WHERE { "+
		   " ?Drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Drug> ."+
		   " ?y <http://www.w3.org/2002/07/owl#sameAs> ?Drug ."+
		   " ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1> ?y ."+
		   " ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug2> ?IntDrug ."+
		   " ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/text> ?IntEffect . "+
		"}";
		queries.add(ls3);
		String ls4 = "SELECT ?drugDesc ?cpd ?equation WHERE {"+
			   "?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics> ."+
				"   ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?cpd ."+
				 "  ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/description> ?drugDesc ."+
				  " ?enzyme <http://bio2rdf.org/ns/kegg#xSubstrate> ?cpd ."+
				   "?enzyme <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Enzyme> ."+
				   "?reaction <http://bio2rdf.org/ns/kegg#xEnzyme> ?enzyme ."+
				   "?reaction <http://bio2rdf.org/ns/kegg#equation> ?equation . "+
				"}";
		queries.add(ls4);
	String ls5 = "SELECT $drug $keggUrl $chebiImage WHERE {"+
			  "$drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs> ."+
				"  $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> $keggDrug ."+
				"  $keggDrug <http://bio2rdf.org/ns/bio2rdf#url> $keggUrl ."+
				 " $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/genericName> $drugBankName ."+
				 " $chebiDrug <http://purl.org/dc/elements/1.1/title> $drugBankName ."+
				 " $chebiDrug <http://bio2rdf.org/ns/bio2rdf#image> $chebiImage ."+
				"}" ;
	queries.add(ls5);
		String ls6 = "SELECT ?drug ?title WHERE { "+
			 "?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/micronutrient> ."+
				" ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> ?id ."+
				" ?keggDrug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Drug> ."+
				" ?keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> ?id ."+
				" ?keggDrug <http://purl.org/dc/elements/1.1/title> ?title ."+
			"}";
		queries.add(ls6);
		String ls7 = "SELECT $drug $transform $mass WHERE {  "+
		 	"{ $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/affectedOrganism>  'Humans and other mammals'."+
		 	" 	  $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> $cas ."+
		 	 "	  $keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> $cas ."+
		 	 "	  $keggDrug <http://bio2rdf.org/ns/bio2rdf#mass> $mass"+
		 	 "	      FILTER ( $mass > '5' )"+
		 	 "	} "+
		 	 "	  OPTIONAL { $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/biotransformation> $transform . } "+
		 	"}";
		queries.add(ls7);
		//------------------------------------ld-----------------------------------
		String ld1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
			"SELECT * WHERE {"+
			"?paper <http://data.semanticweb.org/ns/swc/ontology#isPartOf> <http://data.semanticweb.org/conference/iswc/2008/poster_demo_proceedings> ."+
			"?paper <http://swrc.ontoware.org/ontology#author> ?p ."+
			"?p rdfs:label ?n ."+
			"}";
		queries.add(ld1);
		String ld2 = "SELECT * WHERE {" +
				"?proceedings <http://data.semanticweb.org/ns/swc/ontology#relatedToEvent>  <http://data.semanticweb.org/conference/eswc/2010> ." +
				"?paper <http://data.semanticweb.org/ns/swc/ontology#isPartOf> ?proceedings ." +
				"?paper <http://swrc.ontoware.org/ontology#author> ?p ." +
				"}";
		queries.add(ld2);
		String ld3 = "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"SELECT * WHERE {" +
				"?paper <http://data.semanticweb.org/ns/swc/ontology#isPartOf> <http://data.semanticweb.org/conference/iswc/2008/poster_demo_proceedings> ." +
				"?paper <http://swrc.ontoware.org/ontology#author> ?p ." +
				"?p owl:sameAs ?x ." +
				"?p rdfs:label ?n ." +
				"}";
		queries.add(ld3);
		String ld4 = "SELECT * WHERE {" +
				"?role <http://data.semanticweb.org/ns/swc/ontology#isRoleAt> <http://data.semanticweb.org/conference/eswc/2010> ." +
				"?role <http://data.semanticweb.org/ns/swc/ontology#heldBy> ?p ." +
				"?paper <http://swrc.ontoware.org/ontology#author> ?p ." +
				"?paper <http://data.semanticweb.org/ns/swc/ontology#isPartOf> ?proceedings ." +
				"?proceedings <http://data.semanticweb.org/ns/swc/ontology#relatedToEvent>  <http://data.semanticweb.org/conference/eswc/2010> ." +
				"}";
		queries.add(ld4);
		String ld5 = "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
				"PREFIX dbprop: <http://dbpedia.org/property/>" +
				"PREFIX dbowl: <http://dbpedia.org/ontology/>" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
				"PREFIX factbook: <http://www4.wiwiss.fu-berlin.de/factbook/ns#>" +
				"PREFIX mo: <http://purl.org/ontology/mo/>" +
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
				"PREFIX fb: <http://rdf.freebase.com/ns/>" +
				"SELECT * WHERE {" +
				"?a dbowl:artist dbpedia:Michael_Jackson ." +
				"?a rdf:type dbowl:Album ." +
				"?a foaf:name ?n ." +
				"}";
		queries.add(ld5);
		String ld6 = "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
				"PREFIX dbowl: <http://dbpedia.org/ontology/>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX linkedMDB: <http://data.linkedmdb.org/resource/>" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
				"SELECT * WHERE {" +
				"?director dbowl:nationality dbpedia:Italy ." +
				"?film dbowl:director ?director." +
				"?x owl:sameAs ?film ." +
				"?x foaf:based_near ?y ." +
				"?y <http://www.geonames.org/ontology#officialName> ?n ." +
				"}";
		queries.add(ld6);
		String ld7 = "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
				"PREFIX dbowl: <http://dbpedia.org/ontology/>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX linkedMDB: <http://data.linkedmdb.org/resource/>" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
				"PREFIX gn: <http://www.geonames.org/ontology#>" +
				"SELECT * WHERE {" +
				"?x gn:parentFeature <http://sws.geonames.org/2921044/> ." +
				"?x gn:name ?n ." +
				"}";
		queries.add(ld7);
		String ld8 = "PREFIX kegg: <http://bio2rdf.org/ns/kegg#>" +
				"PREFIX drugbank: <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
				"SELECT * WHERE {" +
				"?drug drugbank:drugCategory <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/micronutrient> ." +
				"?drug drugbank:casRegistryNumber ?id ." +
				"?drug owl:sameAs ?s ." +
				"?s foaf:name ?o ." +
				"?s skos:subject ?sub ." +
				"}";
		queries.add(ld8);
		String ld9 = "PREFIX geo-ont: <http://www.geonames.org/ontology#>" +
				"PREFIX dbpedia: <http://dbpedia.org/resource/>" +
				"PREFIX dbprop: <http://dbpedia.org/property/>" +
				"PREFIX dbowl: <http://dbpedia.org/ontology/>" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
				"PREFIX factbook: <http://www4.wiwiss.fu-berlin.de/factbook/ns#>" +
				"PREFIX mo: <http://purl.org/ontology/mo/>" +
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
				"SELECT * WHERE {" +
				"?x skos:subject <http://dbpedia.org/resource/Category:FIFA_World_Cup-winning_countries> ." +
				"?p dbowl:managerClub ?x ." +
				"?p foaf:name \"Luiz Felipe Scolari\" @en." +
						"}";
		queries.add(ld9);
		String ld10 = "PREFIX dbpedia: <http://dbpedia.org/resource/>" +
				"PREFIX dbprop: <http://dbpedia.org/property/>" +
				"PREFIX dbowl: <http://dbpedia.org/ontology/>" +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
				"PREFIX factbook: <http://www4.wiwiss.fu-berlin.de/factbook/ns#>" +
				"SELECT * WHERE {" +
				"?n skos:subject <http://dbpedia.org/resource/Category:Chancellors_of_Germany> ." +
				"?n owl:sameAs ?p2 ." +
				"?p2 <http://data.nytimes.com/elements/latest_use> ?u ." +
				"}";
		queries.add(ld10);
		String ld11 = "PREFIX geo-ont: <http://www.geonames.org/ontology#>" +
				"PREFIX dbpedia: <http://dbpedia.org/resource/>" +
				"PREFIX dbprop: <http://dbpedia.org/property/>" +
				"PREFIX dbowl: <http://dbpedia.org/ontology/>" +
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
				"PREFIX factbook: <http://www4.wiwiss.fu-berlin.de/factbook/ns#>" +
				"PREFIX mo: <http://purl.org/ontology/mo/>" +
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
				"SELECT * WHERE {" +
				"?x dbowl:team dbpedia:Eintracht_Frankfurt ." +
				"?x rdfs:label ?y ." +
				"?x dbowl:birthDate ?d ." +
				"?x dbowl:birthPlace ?p ." +
				"?p rdfs:label ?l ." +
				"} ";
		queries.add(ld11);
		//-----------------------------------------------------------------

		FedX fed = FederationManager.getInstance().getFederation();
		List<Endpoint> members = fed.getMembers();
		Cache cache =FederationManager.getInstance().getCache();
		int no=1;
		for(String query: queries)
		{
		
		HibiscusSourceSelection sourceSelection = new HibiscusSourceSelection(members,cache, query);
	    HashMap<Integer, List<StatementPattern>> bgpGrps =  BGPGroupGenerator.generateBgpGroups(query);
	  //  System.out.println(DNFgrps)
	    System.out.println(no+":-------------------------------------\n"+query);
	    no++;
	    long startTime = System.currentTimeMillis();	
	    Map<StatementPattern, List<StatementSource>> stmtToSources = sourceSelection.performSourceSelection(bgpGrps);
		System.out.println("Source selection exe time (ms): "+ (System.currentTimeMillis()-startTime));
        int tpsrces = 0; 
		for (StatementPattern stmt : stmtToSources.keySet()) 
          {
        	tpsrces = tpsrces+ stmtToSources.get(stmt).size();
			//System.out.println("-----------\n"+stmt);
			//System.out.println(stmtToSources.get(stmt));
         }
	      System.out.println("Total Triple pattern-wise selected sources after step 2 of HIBISCuS source selection : "+ tpsrces);
		//  FederationManager.getInstance().shutDown();
	      Thread.sleep(1000);
		}	
	  FederationManager.getInstance().shutDown();
		  System.exit(0);
	}
}
