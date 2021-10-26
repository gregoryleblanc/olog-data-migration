package org.phoebus.old.oual;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.lang.Class;

import java.time.Instant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.phoebus.olog.LogRetrieval;
import org.phoebus.olog.entity.Logbook;
import org.phoebus.olog.entity.Tag;
import org.phoebus.olog.entity.Log.LogBuilder;
import org.phoebus.olog.entity.Log;
import org.phoebus.olog.entity.*;
import org.phoebus.olog.entity.Property;
import java.sql.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URI;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.UriBuilder;


@SpringBootApplication
@RestController
public class oualLogRetrieval implements LogRetrieval {

    private final WebResource service;
    private URI ologURI;
    String myUrl = "jdbc:mariadb://localhost/logbook";
    String myUser = "edwards";
    String myPassword = "Edwards";

    public oualLogRetrieval()
    {
        this.ologURI = URI.create("http://localhost:48080");

        DefaultClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getClasses().add(MultiPartWriter.class);
        Client client = Client.create(clientConfig);
        client.setFollowRedirects(true);
        service = client.resource(UriBuilder.fromUri(ologURI).build());
    }

	public static void main(String[] args) {
		SpringApplication.run(oualLogRetrieval.class, args);
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@GetMapping("/logbook")
    public String fetchData(String[] args) {
		String returnStuff = new String();
        try {
            // create our mysql database connection
            String myDriver = "org.mariadb.jdbc.Driver";
            String myUrl = "jdbc:mariadb://localhost/logbook";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "edwards", "Edwards");

            String query = "SELECT name, message, when_posted, ipaddr, private from logbook ORDER BY when_posted ASC LIMIT 1";

            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {
                String name = rs.getString("name");
                String message = rs.getString("message");
                Date dateCreated = rs.getDate("when_posted");
                String ipAddr = rs.getString("ipaddr");
                String priv = rs.getString("private");

                // print the results
                System.out.println(name);
                System.out.println(message);
                System.out.println(dateCreated);
                System.out.println(ipAddr);
                System.out.println(priv);
				returnStuff = name + message;
            }
            st.close();
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
		return returnStuff;
    }

    @Override
    public List<Tag> retrieveTags()
    {
        List<Tag> allTags = new ArrayList<Tag>();
        LocalDateTime currentDate = new LocalDateTime();
        allTags.add(new Tag("importedData" + currentDate.toString()));
        return allTags;
    }
    @Override
    public int retireveLogCount(){
        try {
            // create our mysql database connection
            Class.forName("getLogCount");
            Connection getCount = DriverManager.getConnection(myUrl, myUser, myPassword);
            String countQuery = "SELECT COUNT(*) FROM logbook;";
            Statement countStatement = getCount.createStatement();
            ResultSet countResults = countStatement.executeQuery(countQuery);
            countStatement.close();
        } catch (Exception e){
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
        return 0;
    }
    @Override
    public List<Property> retrieveProperties(){
        List<Property> myProperty = new ArrayList<Property>();
        myProperty.add(new Property());
        return myProperty;
    }
    @Override
    public List<Logbook> retrieveLogbooks(){
        List<Logbook> myLogbook = new ArrayList<Logbook>();
        myLogbook.add(new Logbook("jeoLogbook", null));
        return myLogbook;
    }
    
    @Override
    public List<Log> retrieveLogs(int size, int page){
        String name = "User Name";
        String message = "Message";
        Instant originalDate = Instant.now();
        List<Log> someLogs = new ArrayList<Log>();
        LogBuilder log = Log.LogBuilder.createLog(message);
        log.owner(name);
        log.createDate(originalDate);
        System.out.println(log);
        
        return someLogs;
    }
}
