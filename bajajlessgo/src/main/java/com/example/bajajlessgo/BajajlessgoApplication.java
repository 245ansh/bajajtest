package com.example.bajajlessgo;

import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BajajlessgoApplication implements CommandLineRunner {
    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(BajajlessgoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // First request - Generate webhook
            String registerUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            JSONObject req = new JSONObject();
            req.put("name", "Ansh Tamrakar");
            req.put("regNo", "9425436312");
            req.put("email", "anshtamrakar221107@acropolis.in");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(req.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(registerUrl, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                System.out.println("Failed to generate webhook. Status: " + response.getStatusCode());
                return;
            }
			JSONObject jsonResponse = new JSONObject(response.getBody());
            String webhookUrl = jsonResponse.getString("webhook");
            String accessToken = jsonResponse.getString("accessToken");
			String finalSqlQuery = "SELECT \n" +
                    "    E1.EMP_ID,\n" +
                    "    E1.FIRST_NAME,\n" +
                    "    E1.LAST_NAME,\n" +
                    "    D.DEPARTMENT_NAME,\n" +
                    "    COUNT(E2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT\n" +
                    "FROM \n" +
                    "    EMPLOYEE E1\n" +
                    "JOIN \n" +
                    "    DEPARTMENT D ON E1.DEPARTMENT = D.DEPARTMENT_ID\n" +
                    "LEFT JOIN \n" +
                    "    EMPLOYEE E2 ON E1.DEPARTMENT = E2.DEPARTMENT\n" +
                    "               AND E2.DOB > E1.DOB\n" +
                    "GROUP BY \n" +
                    "    E1.EMP_ID, E1.FIRST_NAME, E1.LAST_NAME, D.DEPARTMENT_NAME\n" +
                    "ORDER BY \n" +
                    "    E1.EMP_ID DESC;";
            JSONObject submission = new JSONObject();
            submission.put("finalQuery", finalSqlQuery);
			HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.setContentType(MediaType.APPLICATION_JSON);
            postHeaders.set("Authorization", accessToken);

            HttpEntity<String> postEntity = new HttpEntity<>(submission.toString(), postHeaders);
            ResponseEntity<String> postResponse = restTemplate.postForEntity(webhookUrl, postEntity, String.class);
            
            if (postResponse.getStatusCode() == HttpStatus.OK) {
                System.out.println("Submission successful! Response: " + postResponse.getBody());
            } else {
                System.out.println("Submission failed. Status: " + postResponse.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}