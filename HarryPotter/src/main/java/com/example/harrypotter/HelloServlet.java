/**
 * @Author: Tanvi Murke
 * Andrew ID: tmurke
 */
package com.example.harrypotter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.bson.Document;

@WebServlet(name = "helloServlet", urlPatterns = {"/hello-servlet", "/dashboard"})
public class HelloServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //initialize output
        String output="";
        //get house from user inout
        String house = request.getParameter("house");
        //initialize out
        PrintWriter out = response.getWriter();
        //if url has dashboard then display dashboard
        if(request.getServletPath().contains("/dashboard")){
            //display analysis
            this.analyzeData(request, response);
        }
       else{
            // log data to database
            output = getHouse(house);
            logData(request);
      }
       //send output
        out.print(output);
        out.flush();
        out.close();
    }

    public String getHouse(String house) throws IOException {
        //reference https://medium.com/swlh/getting-json-data-from-a-restful-api-using-java-b327aafb3751
        //set connection
        URL url = new URL("https://wizard-world-api.herokuapp.com/Houses");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        //Getting the response code
        int responsecode = conn.getResponseCode();
        //check the responsecode
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + responsecode);
        }
        //initialize buffered reader
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        //get result
        String output;
        StringBuilder res = new StringBuilder();
        while ((output = br.readLine()) != null) {
            res.append(output);
        }
        //return result
        //returns the whole output as the app uses all the parameters
        return res.toString();
    }

    public void destroy() {
    }

    private void logData(HttpServletRequest request){
        //reference: https://stackoverflow.com/questions/22463062/how-can-i-parse-format-dates-with-localdatetime-java-8
        //get timestamp
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        //get phone model
        String phoneModel = request.getHeader("User-Agent");
        // get input parameter
        String house = request.getParameter("house");
        //get response method
        String method = request.getMethod();
        //get response code
        int responseStatusCode = HttpServletResponse.SC_OK;
        //reference: https://github.com/CMU-Heinz-95702/Project4-S23 (MongoDB part for connection)
        // create MongoDB client
        ConnectionString connectionString = new ConnectionString("mongodb+srv://tmurke:Tanvimurke98@cluster0.zbvwy6g.mongodb.net/?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        System.out.println("Connected to MongoDB!");
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> collection = database.getCollection("logData");

        // create log document
        Document logDoc = new Document()
                .append("timestamp", dtf.format(now))
                .append("phoneModel", phoneModel)
                .append("house", house)
                .append("apiRequest", "https://wizard-world-api.herokuapp.com/Houses")
                .append("responseStatusCode", responseStatusCode)
                .append("method", method);

        // insert log document to MongoDB
        collection.insertOne(logDoc);
        // close MongoDB client
        mongoClient.close();
    }
    private void analyzeData(HttpServletRequest request, HttpServletResponse response) {
        //reference: https://github.com/CMU-Heinz-95702/Project4-S23 (MongoDB part for connection)
        // create MongoDB client
        ConnectionString connectionString = new ConnectionString("mongodb+srv://tmurke:Tanvimurke98@cluster0.zbvwy6g.mongodb.net/?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        System.out.println("Connected to MongoDB!");
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> collection = database.getCollection("logData");

        // perform analysis operations
        //get top houses
        List<Document> houseDocuments = collection.aggregate(List.of(
                new Document("$group", new Document("_id", "$house")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)),
                new Document("$limit", 1)
        )).into(new ArrayList<>());

        //get top response code count
        List<Document> topStatusCodeCount = collection.aggregate(List.of(
                new Document("$group", new Document("_id", "$responseStatusCode")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)),
                new Document("$limit", 1)
        )).into(new ArrayList<>());

        //get top phone model count
        List<Document> deviceUsageDocuments = collection.aggregate(List.of(
                new Document("$group", new Document("_id", "$phoneModel")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)),
                new Document("$limit", 1)
        )).into(new ArrayList<>());

        // get all logs from MongoDB and store them in a List<Document> object
        List<Document> allLogs = new ArrayList<Document>();
        //add documents
        FindIterable<Document> docList = collection.find();
        for(Document doc: docList) {
            allLogs.add(doc);
        }
        // close MongoDB client
        mongoClient.close();
        // set the allLogs object as an attribute of the request object
        request.setAttribute("allLogs", allLogs);

        //get house name and it's count from document
        if (houseDocuments != null) {
            if (houseDocuments.size() > 0) {
                Document topHouse = houseDocuments.get(0);
                String houseName = topHouse.getString("_id") ;
                int houseCount = topHouse.getInteger("count");
                //set this and send it to dashboard
                request.setAttribute("houseName", houseName);
                request.setAttribute("houseCount", houseCount);
            }
        }

        //get status code and it's count from document
        if (topStatusCodeCount != null) {
            if (topStatusCodeCount.size() > 0) {
                int topStatusCode = topStatusCodeCount.get(0).getInteger("_id");
                int codeCount = topStatusCodeCount.get(0).getInteger("count");
                //set this and send it to dashboard
                request.setAttribute("topStatusCode", topStatusCode);
                request.setAttribute("codeCount", codeCount);
            }
        }

        //get device details and it's count from document
        if (deviceUsageDocuments != null) {
            if (deviceUsageDocuments.size() > 0) {
                Document topDeviceUsage = deviceUsageDocuments.get(0);
                String phoneModel = topDeviceUsage.getString("_id");
                int phoneCount = topDeviceUsage.getInteger("count");
                //set this and send it to dashboard
                request.setAttribute("phoneModel", phoneModel);
                request.setAttribute("phoneCount", phoneCount);
            }
        }

        // forward to dashboard.jsp
        RequestDispatcher dispatcher = request.getRequestDispatcher("dashboard.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

}