package com.dulno.chatgpt.action.prompt;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTRequestFactory;
import com.dulno.workflow.action.Action;
import com.dulno.workflow.action.ActionContentDatabaseTable;
import com.dulno.workflow.action.ActionInformation;
import com.dulno.core.database.*;
import com.dulno.workflow.component.input.InputComponentDataType;
import com.dulno.workflow.component.input.InputComponentSelect;
import com.dulno.workflow.component.input.InputComponentVariable;
import com.dulno.workflow.component.output.OutputComponentVariable;
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
    contentColumns.add(DatabaseColumn.create("ownerId", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("chatgptId", DatabaseDataType.UUID));
    contentColumns.add(DatabaseColumn.create("model", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("maxTokens", DatabaseDataType.TEXT));
    contentColumns.add(DatabaseColumn.create("temperature", DatabaseDataType.TEXT));
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
      .withInputVariable(InputComponentVariable.createOptional("chatgpt.action.prompt.input.max.tokens.name",
        "chatGPTMaxTokens", "chatgpt.action.prompt.input.max.tokens.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createOptional("chatgpt.action.prompt.input.temperature.name",
        "chatGPTTemperature", "chatgpt.action.prompt.input.temperature.description", InputComponentDataType.TEXT))
      .withInputVariable(InputComponentVariable.createRequired("chatgpt.action.prompt.input.prompt.name",
        "chatGPTPrompt", "chatgpt.action.prompt.input.prompt.description", InputComponentDataType.TEXT))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.response", "chatGPTResponse"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.prompt.tokens", "chatGPTPromptTokens"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.completion.tokens", "chatGPTCompletionTokens"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.total.tokens", "chatGPTTotalTokens"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.max.tokens", "chatGPTMaxTokens"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.temperature", "chatGPTTemperature"))
      .withOutputVariable(OutputComponentVariable.create("chatgpt.action.prompt.output.prompt", "chatGPTPrompt"))
      .build();
  }

  @Override
  public void initialize() {
    contentDatabaseTable.createIfNotExists();
  }

  @Override
  public CompletableFuture<Void> insert(
    UUID actionId, UUID ownerId, Map<String, Object> content
  ) {
    var maxTokens = content.get("chatGPTMaxTokens");
    var temperature = content.get("chatGPTTemperature");
    return contentDatabaseTable.insertContent(actionId, DatabaseRow.of(ownerId,
      UUID.fromString((String) content.get("chatGPTIdentifier")),
      content.get("chatGPTModel"), maxTokens == null ? "" : maxTokens,
      temperature == null ? "" : temperature, content.get("chatGPTPrompt")));
  }

  @Override
  public CompletableFuture<Map<String, Object>> findContent(UUID actionId) {
    return contentDatabaseTable.findContent(actionId).thenApply(row ->
      Map.of("chatGPTIdentifier", row.findCell(2).uuidValue().toString(),
        "chatGPTModel", row.findCell(3).stringValue(),
        "chatGPTMaxTokens", row.findCell(4).stringValue(),
        "chatGPTTemperature", row.findCell(5).stringValue(),
        "chatGPTPrompt", row.findCell(6).stringValue()));
  }

  @Override
  public CompletableFuture<ChatGPTPromptActionExecutor> build(UUID actionId) {
    return contentDatabaseTable.findContent(actionId)
      .thenApply(content -> ChatGPTPromptActionExecutor.create(
        chatGPTDatabaseTable, chatGPTRequestFactory,
        content.findCell(1).uuidValue(), content.findCell(2).uuidValue(),
        content.findCell(3).stringValue(), content.findCell(4).stringValue(),
        content.findCell(5).stringValue(), content.findCell(6).stringValue()));
  }

  @Override
  public CompletableFuture<Void> delete(UUID actionId) {
    return contentDatabaseTable.deleteContent(actionId);
  }
}