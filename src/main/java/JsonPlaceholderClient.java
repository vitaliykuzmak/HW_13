import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

class JsonPlaceholderClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    private static String sendRequest(String urlString, String method, String jsonData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");

        if (jsonData != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonData.getBytes("utf-8"));
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static String createUser(String jsonData) throws IOException {
        return sendRequest(BASE_URL + "/users", "POST", jsonData);
    }

    public static String updateUser(int id, String jsonData) throws IOException {
        return sendRequest(BASE_URL + "/users/" + id, "PUT", jsonData);
    }

    public static int deleteUser(int id) throws IOException {
        return sendRequest(BASE_URL + "/users/" + id, "DELETE", null).length();
    }

    public static String getAllUsers() throws IOException {
        return sendRequest(BASE_URL + "/users", "GET", null);
    }

    public static String getUserById(int id) throws IOException {
        return sendRequest(BASE_URL + "/users/" + id, "GET", null);
    }

    public static String getUserByUsername(String username) throws IOException {
        return sendRequest(BASE_URL + "/users?username=" + username, "GET", null);
    }

    public static void saveCommentsOfLastPost(int userId) throws IOException {
        String postsJson = sendRequest(BASE_URL + "/users/" + userId + "/posts", "GET", null);
        int lastPostId = getLastPostId(postsJson);
        String commentsJson = sendRequest(BASE_URL + "/posts/" + lastPostId + "/comments", "GET", null);
        String fileName = "user-" + userId + "-post-" + lastPostId + "-comments.json";
        Files.write(Paths.get(fileName), commentsJson.getBytes());
        System.out.println("Comments saved to " + fileName);
    }

    private static int getLastPostId(String postsJson) {
        JSONArray posts = new JSONArray(postsJson);
        int maxId = -1;
        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = posts.getJSONObject(i);
            int id = post.getInt("id");
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId;
    }

    public static String getOpenTodos(int userId) throws IOException {
        String todosJson = sendRequest(BASE_URL + "/users/" + userId + "/todos", "GET", null);
        JSONArray todos = new JSONArray(todosJson);
        JSONArray openTodos = new JSONArray();

        for (int i = 0; i < todos.length(); i++) {
            JSONObject todo = todos.getJSONObject(i);
            if (!todo.getBoolean("completed")) {
                openTodos.put(todo);
            }
        }
        return openTodos.toString();
    }

    public static void main(String[] args) throws IOException {
        String newUserJson = new JSONObject()
                .put("name", "John Doe")
                .put("username", "johndoe")
                .put("email", "johndoe@example.com")
                .put("address", new JSONObject()
                        .put("street", "Kulas Light")
                        .put("suite", "Apt. 556")
                        .put("city", "Gwenborough")
                        .put("zipcode", "92998-3874"))
                .put("phone", "1-770-736-8031 x56442")
                .put("website", "hildegard.org")
                .toString();

        String createdUser = createUser(newUserJson);
        System.out.println("Created User: " + createdUser);

        saveCommentsOfLastPost(1);

        String openTodos = getOpenTodos(1);
        System.out.println("Open Todos for User 1: " + openTodos);

        System.exit(0);
    }
}
