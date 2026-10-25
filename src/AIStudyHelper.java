
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

    private static final String OLLAMA_URL =
            "http://localhost:11434/api/chat";

    private static final String MODEL = "qwen2.5:3b";

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        System.out.println("==============================");
        System.out.println("     JAVA AI STUDY HELPER");
        System.out.println("==============================");
        System.out.println("AI Model: " + MODEL);
        System.out.println("Ask a question or type exit.");

        while (true) {

            System.out.print("\nEnter your question: ");
            String question = scanner.nextLine().trim();

            if (question.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (question.isEmpty()) {
                System.out.println("Please enter a question.");
                continue;
            }

            try {
                String answer = askAI(client, question);

                System.out.println("\nAI Answer:");
                System.out.println(answer);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Request interrupted.");
                break;

            } catch (Exception e) {
                System.out.println("Unable to get AI response.");
                System.out.println("Error: " + e.getMessage());
                System.out.println(
                        "Check that Ollama is running and the model is installed."
                );
            }
        }

        scanner.close();
    }

    private static String askAI(
            HttpClient client, String question) throws Exception {

        JsonObject requestBody = new JsonObject();

        requestBody.addProperty("model", MODEL);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty(
                "content",
                "You are a helpful study assistant. "
                        + "Explain concepts in simple English "
                        + "and provide examples."
        );

        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", question);

        messages.add(userMessage);

        requestBody.add("messages", messages);
        requestBody.addProperty("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        requestBody.toString()))
                .build();

        System.out.println("AI is thinking...");

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Ollama HTTP error: " + response.statusCode()
                            + " " + response.body()
            );
        }

        JsonObject result = JsonParser
                .parseString(response.body())
                .getAsJsonObject();

        return result.getAsJsonObject("message")
                .get("content")
                .getAsString();
    }
}
