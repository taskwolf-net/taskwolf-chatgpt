package com.dulno.chatgpt.structure;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class ChatGPTRequest {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final HttpClient httpClient;
  private final UUID chatGPTId;

  public CompletableFuture<HttpResponse<String>> send(
    String url, String method, Map<String, Object> body
  ) {
    return send(url, method, new JSONObject(body).toString());
  }

  public CompletableFuture<HttpResponse<String>> send(
    String url, String method, String body
  ) {
    return send(url, method, body, "application/json");
  }

  public CompletableFuture<HttpResponse<String>> send(
    String url, String method, String body, String contentType
  ) {
    return chatGPTDatabaseTable.findChatGPT(chatGPTId)
      .thenCompose(chatGPT -> send(chatGPT, url, method, body, contentType));
  }

  private CompletableFuture<HttpResponse<String>> send(
    ChatGPT chatGPT, String url, String method, String body, String contentType
  ) {
    var requestBuilder = HttpRequest.newBuilder()
      .uri(URI.create("https://api.openai.com/v1/" + url))
      .method(method, HttpRequest.BodyPublishers.ofString(body));
    requestBuilder.setHeader("Authorization", "Bearer " + chatGPT.accessToken());
    requestBuilder.setHeader("Content-Type", contentType);
    var httpRequest = requestBuilder.build();
    return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
  }
}
