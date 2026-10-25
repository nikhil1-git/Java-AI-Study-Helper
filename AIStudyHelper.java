
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class AIStudyHelper {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        HttpClient client = HttpClient.newHttpClient();

        System.out.println("==============================");
        System.out.println("      JAVA AI STUDY HELPER");
        System.out.println("==============================");
        System.out.println("Ask any question to the AI.");
        System.out.println("Type 'exit' to close.");

        while (true) {
            System.out.print("\nEnter your question: ");
            String question = sc.nextLine().trim();

            if (question.equalsIgnoreCase("exit")) {
                System.out.println("ThankYou, GoodBye!");
                break;
            }

            if (question.isEmpty()) {
                System.out.println("Please enter a question.");
                continue;
            }

            try {
                JsonObject body = new JsonObject();
                body.addProperty("model", "qwen2.5:3b");

                JsonArray messages = new JsonArray();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", question);
                messages.add(message);

                body.add("messages", messages);
                body.addProperty("stream", false);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:11434/api/chat"))
                        .timeout(Duration.ofMinutes(5))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                body.toString()))
                        .build();

                System.out.println("\nAI is thinking...");

                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    System.out.println("AI error: " + response.body());
                    continue;
                }

                JsonObject result = JsonParser
                        .parseString(response.body())
                        .getAsJsonObject();

                String answer = result
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString();

                System.out.println("\nAI Answer:\n" + answer);

            } catch (Exception e) {
                System.out.println("Could not connect to Ollama.");
                System.out.println("Error: " + e.getMessage());
            }
        }

        sc.close();
    }
}
