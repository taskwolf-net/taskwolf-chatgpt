package com.dulno.chatgpt.action.prompt;

import com.dulno.chatgpt.structure.ChatGPT;
import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTRequestFactory;
import com.dulno.workflow.action.ActionExecutor;
import com.dulno.workflow.action.ActionResult;
import com.dulno.workflow.placeholder.PlaceholderDissolve;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChatGPTPromptActionExecutor implements ActionExecutor {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final ChatGPTRequestFactory chatGPTRequestFactory;
  private final UUID ownerId;
  private final UUID chatGPTId;
  private final String model;
  private String maxTokens;
  private String temperature;
  private String prompt;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    maxTokens = dissolve.dissolve(maxTokens);
    temperature = dissolve.dissolve(temperature);
    prompt = dissolve.dissolve(prompt);
    return chatGPTDatabaseTable.chatGPTExists(chatGPTId).thenCompose(this::execute);
  }

  private CompletableFuture<ActionResult> execute(boolean chatGPTExists) {
    if (!chatGPTExists) {
      return ActionResult.futureFailure("chatgpt.action.prompt.failure.chatgpt.not.found");
    }
    return chatGPTDatabaseTable.findChatGPT(chatGPTId).thenCompose(this::execute);
  }

  private CompletableFuture<ActionResult> execute(ChatGPT chatGPT) {
    if (!chatGPT.ownerId().equals(ownerId)) {
      return ActionResult.futureFailure("chatgpt.action.prompt.failure.chatgpt.not.found");
    }
    var body = Maps.<String, Object>newHashMap();
    body.put("model", model);
    body.put("messages", Lists.newArrayList(Map.of("role", "user",
      "content", prompt)));
    body.putAll(parseMaxTokens());
    body.putAll(parseTemperature());
    return chatGPTRequestFactory.create(chatGPTId)
      .send("/chat/completions", "POST", body).thenApply(this::execute);
  }

  private Map<String, Object> parseMaxTokens() {
    try {
      return Map.of("max_completion_tokens", Integer.parseInt(maxTokens));
    } catch (Exception exception) {
      return Maps.newHashMap();
    }
  }

  private Map<String, Object> parseTemperature() {
    try {
      return Map.of("temperature", Float.parseFloat(temperature));
    } catch (Exception exception) {
      return Maps.newHashMap();
    }
  }

  private ActionResult execute(HttpResponse<String> response) {
    if (response.statusCode() != 200) {
      return ActionResult.failure(new JSONObject(response.body())
        .getJSONObject("error").getString("message"));
    }
    return ActionResult.success(buildInformation(new JSONObject(response.body())));
  }

  private Map<String, Object> buildInformation(JSONObject response) {
    var information = Maps.<String, Object>newHashMap();
    information.put("chatGPTResponse", response.getJSONArray("choices")
      .getJSONObject(0).getJSONObject("message").getString("content"));
    var usage = response.getJSONObject("usage");
    information.put("chatGPTPromptTokens", usage.getLong("prompt_tokens"));
    information.put("chatGPTCompletionTokens", usage.getLong("completion_tokens"));
    information.put("chatGPTTotalTokens", usage.getLong("total_tokens"));
    information.put("chatGPTMaxTokens", maxTokens);
    information.put("chatGPTTemperature", temperature);
    information.put("chatGPTPrompt", prompt);
    return information;
  }
}
