package ca.cgta.couchdb.tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

public class UhnDataPurger {
	
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(UhnDataPurger.class);
	
    
    
	public static void main(String[] args) throws Exception {

	    //parse command line arguments        

        //create the command line parser
        CommandLineParser parser = new PosixParser();

        //create the Options
        Options options = new Options();

        options.addOption("u", "url", true, "URL of the couchDb instance");
        options.addOption("d", "databaseName", true, "Name of the couchDb database");
        options.addOption("c", "clientName", true, "couchDb account username");
        options.addOption("p", "password", true, "couchDb account pwd");
        
        CommandLine cmdLine = null;
        // parse the command line arguments
        cmdLine = parser.parse(options, args);

          
        String url = cmdLine.getOptionValue("u", "http://uhnvprx01t.uhn.ca:5984");
        String dbName = cmdLine.getOptionValue("d", "cgta_input_test_db");
        //String dbName = cmdLine.getOptionValue("d", "neal_test_db");
        String user = cmdLine.getOptionValue("c", "admin");
        String pwd = cmdLine.getOptionValue("p", "denali6194");
        
        HttpClient httpClient = new StdHttpClient.Builder().url(url).username(user).password(pwd).connectionTimeout(10000).build();
        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);        
        StdCouchDbConnector connector = new StdCouchDbConnector(dbName, dbInstance);
                
        AllDataPurger.purgeAllMatchingDocs(connector, "1.3.6.1.4.1.12201");
		
		
	}


    
	
	
	
	
}
