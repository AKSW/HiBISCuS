package org.aksw.simba.hibiscus.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.hibiscus.util.FedSumGenerator;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

/**
 * Test FedSummaries Generator for a set of SPARQL endpoints
 * @author Saleem
 */
public class TestFedSumGenerator {
	
	public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
	List<String> endpoints = 	(Arrays.asList(
			 "http://localhost:8890/sparql",
			 "http://localhost:8891/sparql",
			 "http://localhost:8892/sparql",
			 "http://localhost:8893/sparql",
			 "http://localhost:8894/sparql",
			 "http://localhost:8895/sparql",
			 "http://localhost:8896/sparql",
			 "http://localhost:8897/sparql",
			 "http://localhost:8898/sparql",
			 "http://localhost:8899/sparql"
			));

	String outputFile = "D:/workspace/HiBISCus/summaries/OntoSum-UOBM.n3";
	FedSumGenerator generator = new FedSumGenerator(outputFile);
	long startTime = System.currentTimeMillis();
	generator.generateSummaries(endpoints,null);
	System.out.println("Data Summaries Generation Time (sec): "+ (System.currentTimeMillis()-startTime)/1000);
	System.out.print("Data Summaries are secessfully stored at "+ outputFile);
	}

}
 