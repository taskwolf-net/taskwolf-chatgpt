package com.dulno.chatgpt.select;

import com.dulno.chatgpt.structure.ChatGPT;
import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTRequestFactory;
import com.dulno.core.user.User;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentSelectEntry;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public class ModelComponentSelect implements InputComponentSelect {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final ChatGPTRequestFactory chatGPTRequestFactory;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    try {
      var chatGPTId = UUID.fromString(previousInputs.get("chatGPTIdentifier"));
      return chatGPTDatabaseTable.chatGPTExists(chatGPTId).thenCompose(exists ->
        checkChatGPTExistence(chatGPTId, target, exists));
    } catch (Exception exception) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
  }

  private CompletableFuture<List<InputComponentSelectEntry>> checkChatGPTExistence(
    UUID chatGPTId, UUID target, boolean chatGPTExists
  ) {
    if (!chatGPTExists) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return chatGPTDatabaseTable.findChatGPT(chatGPTId)
      .thenCompose(chatGPT -> checkChatGPTAccess(chatGPT, target));
  }

  private CompletableFuture<List<InputComponentSelectEntry>> checkChatGPTAccess(
    ChatGPT chatGPT, UUID target
  ) {
    if (!chatGPT.ownerId().equals(target)) {
      return CompletableFuture.completedFuture(Lists.newArrayList());
    }
    return chatGPTRequestFactory.create(chatGPT.id())
      .send("/models", "GET", "")
      .thenApply(this::parseModels);
  }

  private List<InputComponentSelectEntry> parseModels(
    HttpResponse<String> response
  ) {
    if (response.statusCode() != 200) {
      return Lists.newArrayList();
    }
    var models = new JSONObject(response.body()).getJSONArray("data");
    var result = Lists.<InputComponentSelectEntry>newArrayList();
    for (var i = 0; i < models.length(); i++) {
      var model = models.getJSONObject(i);
      result.add(InputComponentSelectEntry.create(model.getString("id"),
        model.getString("id")));
    }
    return result;
  }
}
