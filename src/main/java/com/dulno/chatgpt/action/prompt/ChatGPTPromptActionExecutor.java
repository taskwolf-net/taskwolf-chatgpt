package com.dulno.chatgpt.action.prompt;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTRequestFactory;
import com.dulno.core.action.ActionExecutor;
import com.dulno.core.action.ActionResult;
import com.dulno.core.workflow.placeholder.PlaceholderDissolve;
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
  private final UUID chatGPTId;
  private final String model;
  private String prompt;

  @Override
  public CompletableFuture<ActionResult> execute(Map<String, Object> information) {
    var dissolve = PlaceholderDissolve.create(information);
    prompt = dissolve.dissolve(prompt);
    return chatGPTDatabaseTable.chatGPTExists(chatGPTId).thenCompose(this::execute);
  }

  private CompletableFuture<ActionResult> execute(boolean gitlabExists) {
    if (!gitlabExists) {
      return ActionResult.futureFailure("chatgpt.action.prompt.failure.chatgpt.not.found");
    }
    var body = Map.<String, Object>of("model", model, "messages",
      Lists.newArrayList(Map.of("role", "user", "content", prompt)));
    return chatGPTRequestFactory.create(chatGPTId)
      .send("/chat/completions", "POST", body).thenApply(this::execute);
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
    information.put("chatGPTPrompt", prompt);
    information.put("chatGPTResponse", response.getJSONArray("choices")
      .getJSONObject(0).getJSONObject("message").getString("content"));
    information.put("chatGPTTokens", response.getJSONObject("usage")
      .getLong("total_tokens"));
    return information;
  }
}
