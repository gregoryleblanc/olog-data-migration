package org.phoebus.old.oual;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.lang.Class;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.joda.time.LocalDate;

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
import java.util.Set;
import java.util.HashSet;
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
    String myDriver = "org.mariadb.jdbc.Driver";
    String myUrl = "jdbc:mariadb://localhost/logbook";
    String myUser = "edwards";
    String myPassword = "Edwards";

    public oualLogRetrieval() {
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

    @Override
    public List<Tag> retrieveTags() {
        List<Tag> allTags = new ArrayList<Tag>();
        allTags.add(new Tag("importedData" + (new LocalDate()).toString()));
        allTags.add(new Tag("jeoLogbook"));
        return allTags;
    }

    @Override
    public int retireveLogCount() {
        int count = 0;
        try {
            // create our mysql database connection
            Class.forName(myDriver);
            Connection getCount = DriverManager.getConnection(myUrl, myUser, myPassword);
            String countQuery = "SELECT COUNT(*) AS rowcount FROM logbook;";
            Statement countStatement = getCount.createStatement();
            ResultSet countResults = countStatement.executeQuery(countQuery);
            countResults.next();
            count = countResults.getInt("rowcount");
            countStatement.close();
            // countResults.getInt(columnLabel);
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
        return count;
    }

    @Override
    public List<Property> retrieveProperties() {
        List<Property> myProperty = new ArrayList<Property>();
        myProperty.add(new Property());
        return myProperty;
    }

    @Override
    public List<Logbook> retrieveLogbooks() {
        List<Logbook> myLogbook = new ArrayList<Logbook>();
        myLogbook.add(new Logbook("jeoLogbook", null));
        myLogbook.add(new Logbook("Operations", null));
        return myLogbook;
    }

    @GetMapping("/OUAL")
    @Override
    public List<Log> retrieveLogs(int size, int page) {
        // String name = "noName";
        // String logMessage = "NoMessage";
        // String ipAddr = "NoIP";
        // Boolean isPrivate = false;
        // Instant originalDate = Instant.now();
        List<Log> someLogs = new ArrayList<Log>();
        Long counter = (long) 0;
        Long yearMinutes = ChronoUnit.MINUTES.between(java.time.LocalDateTime.of(java.time.LocalDateTime.now().getYear(), 1, 1, 0, 0), java.time.LocalDateTime.now());

        try {
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, myUser, myPassword);
            String query = "SELECT name, message, when_posted, ipaddr, private from logbook ORDER BY when_posted ASC LIMIT 10";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            Attribute ipAddress;
            Attribute isPrivate;
            while(rs.next()) {
                counter++;
                LogBuilder log = Log.LogBuilder.createLog();
                Property myProperties = new Property();
                Set<Property> allProperties = new HashSet<Property>();
                log.owner(rs.getString("name"));
                Set<Tag> myTags = new HashSet<Tag>();
                log.description(rs.getString("message"));
                log.createDate(rs.getTimestamp("when_posted").toInstant());
                log.withLogbook(new Logbook("jeoLogbook", null));
                log.withLogbook(new Logbook("Operations", null));
                myTags.add(new Tag("jeoLogbook"));
                myTags.add(new Tag("importedData" + (new LocalDate()).toString()));
                log.setTags(myTags);
                ipAddress = new Attribute("IP Address", rs.getString("ipaddr"));
                isPrivate = new Attribute("Is Private", rs.getString("private"));
                myProperties.addAttributes(ipAddress);
                myProperties.addAttributes(isPrivate);
                myProperties.setName("Imported");
                myProperties.setOwner("leblanc");
                allProperties.add(myProperties);
                log.setProperties(allProperties);
                log.modifyDate(Instant.now());
                log.id(yearMinutes * counter);
                someLogs.add(log.build());
            }
            st.close();
        }
        catch (Exception e) {
            System.err.println("Got an exception! Also, this message sucks.");
            System.err.println(e.getMessage());
        }
        return someLogs;
    }
}
