package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static String house = "";
    static String houseColours = "";
    static String founder = "";
    static String animal = "";
    static String element = "";
    static String ghost = "";
    static String commonRoom = "";
    static List<String> traitNameList = new ArrayList<>();
    public static void main(String[] args) throws IOException, ParseException {

        System.out.println("Enter your house");
        Scanner sc = new Scanner(System.in);
        house = sc.nextLine();

        //reference https://medium.com/swlh/getting-json-data-from-a-restful-api-using-java-b327aafb3751
        URL url = new URL("https://wizard-world-api.herokuapp.com/Houses");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        //Getting the response code
        int responsecode = conn.getResponseCode();

        if (conn.getResponseCode() != 200) {
            System.out.println("Enter another house");
            throw new RuntimeException("Failed : HTTP error code : " + responsecode);

        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        StringBuilder response = new StringBuilder();
        while ((output = br.readLine()) != null) {
            response.append(output);
        }

        String jsonResponse = response.toString();
        int flag = 0;
        JSONParser parser = new JSONParser();
        JSONArray jsonArr = (JSONArray) parser.parse(jsonResponse);
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObj = (JSONObject) jsonArr.get(i);
            String name = (String) jsonObj.get("name");
            if (name.equalsIgnoreCase(house)) {
                flag = 1;
                houseColours = (String) jsonObj.get("houseColours");
                founder = (String) jsonObj.get("founder");
                animal = (String) jsonObj.get("animal");
                element = (String) jsonObj.get("element");
                ghost = (String) jsonObj.get("ghost");
                commonRoom = (String) jsonObj.get("commonRoom");
                JSONArray headsArr = (JSONArray) jsonObj.get("heads");
                for (int j = 0; j < headsArr.size(); j++) {
                    JSONObject headsObj = (JSONObject) headsArr.get(j);
                    String firstName = (String) headsObj.get("firstName");
                    String lastName = (String) headsObj.get("lastName");
                }
                JSONArray traitsArr = (JSONArray) jsonObj.get("traits");
                for (int k = 0; k < traitsArr.size(); k++) {
                    JSONObject traitsObj = (JSONObject) traitsArr.get(k);
                    String traitName = (String) traitsObj.get("name");
                    traitNameList.add(traitName);
                }
            }
        }
        if(flag!=1){
            System.out.println("Enter a valid house name");
        }else{
            System.out.println("House colour is: "+houseColours);
            System.out.println("Founder is: "+founder);
            System.out.println("Animal is: "+animal);
            System.out.println("Element is: "+element);
            System.out.println("Ghost is: "+ghost);
            System.out.println("Common room is: "+commonRoom);
            System.out.println("Traits are: ");
            for(int i = 0; i< traitNameList.size();i++){
                System.out.print(traitNameList.get(i) + "  ");
            }

        }

    }
}

