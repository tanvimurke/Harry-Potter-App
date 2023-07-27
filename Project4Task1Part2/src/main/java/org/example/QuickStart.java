package org.example;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;


public class QuickStart {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a string: ");
        String inputString = scanner.nextLine();

        ConnectionString connectionString = new ConnectionString("mongodb+srv://tmurke:Tanvimurke98@cluster0.zbvwy6g.mongodb.net/?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("test");

      //  String uri = "mongodb+srv://tmurke:Tanvimurke98@cluster0.zbvwy6g.mongodb.net/?retryWrites=true&w=majority";
;
       // try (MongoClient mongoClient = MongoClients.create(uri)) {
       //     MongoDatabase database = mongoClient.getDatabase("sample_mflix");
            MongoCollection<Document> collection = database.getCollection("strings");
        //    Document doc = collection.find(eq("title", "Back to the Future")).first();
        // Create a new document with the input string
        Document document = new Document("string", inputString);

        // Insert the document into the collection
        collection.insertOne(document);

        // Retrieve all documents from the collection
        for (Document d : collection.find()) {
            System.out.println(d.getString("string"));
        }
          /*  if (doc != null) {
                System.out.println(doc.toJson());
            } else {
                System.out.println("No matching documents found.");
            } */
        }
}
