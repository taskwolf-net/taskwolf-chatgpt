package com.dulno.chatgpt.action.prompt;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTRequestFactory;
import com.dulno.core.action.Action;
import com.dulno.core.action.ActionContentDatabaseTable;
import com.dulno.core.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.core.workflow.component.input.InputComponentDataType;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentVariable;
import com.dulno.core.workflow.component.output.OutputComponentVariable;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor(staticName = "create")
public final class ChatGPTPromptAction implements Action<ChatGPTPromptActionExecutor> {
  public static ChatGPTPromptAction create(
    InputComponentSelect chatGPTComponentSelect,
    InputComponentSelect modelComponentSelect,
    ChatGPTDatabaseTable chatGPTDatabaseTable,
    ChatGPTRequestFactory chatGPTRequestFactory,
    DatabaseConnection databaseConnection, DatabaseKeyspace databaseKeyspace
  ) {
    var contentColumns = Lists.<DatabaseColumn>newArrayList();
    contentColumns.add(DatabaseColumn.create("chatgptId", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("model", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("prompt", DatabaseDataType.TEXT));
    return new ChatGPTPromptAction(chatGPTComponentSelect, modelComponentSelect,
      chatGPTDatabaseTable, chatGPTRequestFactory,
      ActionContentDatabaseTable.create(databaseConnection, databaseKeyspace,
        "action_chatgpt_prompt", contentColumns));
  }

  private final InputComponentSelect chatGPTComponentSelect;
  private final InputComponentSelect modelComponentSelect;
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final ChatGPTRequestFactory chatGPTRequestFactory;
  private final ActionContentDatabaseTable contentDatabaseTable;

  @Override
  public String type() {
    return "chatgpt-prompt-action";
  }

  @Override
  public ActionInformation information() {
    return ActionInformation.builder()
      .withName("chatgpt.action.prompt.name")
      .withDescription("chatgpt.action.prompt.description")
      .withInputVariable(InputComponentVariable.createSelect("chatgpt.action.prompt.input.chatgpt.name",
        "chatGPTIdentifier", "chatgpt.action.prompt.input.chatgpt.description", chatGPTComponentSelect))
      .withInputVariable(InputComponentVariable.createSelect("chatgpt.action.prompt.input.model.name",
        "chatGPTModel", "chatgpt.action.prompt.input.model.description", modelComponentSelect))
      .withInputVariable(InputComponentVariable.createRequired("chatgpt.action.prompt.input.prompt.name",
        "chatGPTPrompt", "chatgpt.action.prompt.input.prompt.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.prompt", "chatGPTPrompt"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.response", "chatGPTResponse"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.tokens", "chatGPTTokens"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(UUID actionId, Map<String, Object> content) {
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(
      UUID.fromString((String) content.get("chatGPTIdentifier")),
      content.get("chatGPTModel"), content.get("chatGPTPrompt")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("chatGPTIdentifier", row.findCell(1).uuidValue().toString(),
        "chatGPTModel", row.findCell(2).stringValue(),
        "chatGPTPrompt", row.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<ChatGPTPromptActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId)
      .thenApply(content -> ChatGPTPromptActionExecutor.create(
        chatGPTDatabaseTable, chatGPTRequestFactory,
        content.findCell(1).uuidValue(), content.findCell(2).stringValue(),
        content.findCell(3).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}