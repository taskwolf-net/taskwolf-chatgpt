package com.dulno.chatgpt.structure;

import com.dulno.core.database.*;
import com.dulno.core.database.condition.DatabaseComparison;
import com.dulno.core.database.condition.DatabaseCondition;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ChatGPTDatabaseTable extends DatabaseTable {
  private static final String TABLE_NAME = "chatgpt";

  public static ChatGPTDatabaseTable create(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var columns = Lists.<DatabaseColumn>newArrayList();
    columns.add(DatabaseColumn.create("id", DatabaseDataType.UUID,
      DatabaseColumn.Type.PRIMARY_KEY));
    columns.add(DatabaseColumn.create("owner", DatabaseDataType.UUID));
    columns.add(DatabaseColumn.create("accountName", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("accountType", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("accessToken", DatabaseDataType.TEXT));
    columns.add(DatabaseColumn.create("organization", DatabaseDataType.TEXT));
    return new ChatGPTDatabaseTable(connection, keyspace, TABLE_NAME, columns);
  }

  private ChatGPTDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace, String name,
    List<DatabaseColumn> columns
  ) {
    super(connection, keyspace, name, columns);
  }

  public CompletableFuture<Void> insertChatGPT(ChatGPT chatGPT) {
    return insertChatGPT(chatGPT.id(), chatGPT.ownerId(), chatGPT.accountName(),
      chatGPT.accountType(), chatGPT.accessToken(), chatGPT.organizationId());
  }

  public CompletableFuture<Void> insertChatGPT(
    UUID id, UUID ownerId, String accountName, ChatGPTType type,
    String accessToken, String organizationId
  ) {
    return insert(DatabaseRow.of(id, ownerId, accountName, type.toString(),
      accessToken, organizationId));
  }

  public CompletableFuture<UUID> generateAvailableChatGPTId() {
    var futureResponse = new CompletableFuture<UUID>();
    var id = UUID.randomUUID();
    chatGPTExists(id).thenApply(exists -> exists ?
      generateAvailableChatGPTId().thenApply(futureResponse::complete) :
      CompletableFuture.completedFuture(futureResponse.complete(id)));
    return futureResponse;
  }

  public void deleteChatGPT(UUID id) {
    delete(id);
  }

  public CompletableFuture<Boolean> chatGPTExists(UUID id) {
    return exists(id);
  }

  public CompletableFuture<Boolean> chatGPTExistsByOwner(UUID ownerId) {
    var condition = DatabaseCondition.of(
      DatabaseComparison.create("owner", ownerId));
    return exists(condition);
  }

  public CompletableFuture<ChatGPT> findChatGPT(UUID id) {
    return selectRow(id).thenApply(ChatGPT::of);
  }

  public CompletableFuture<List<ChatGPT>> findChatGPTsOfOwner(UUID ownerId) {
    var condition = DatabaseCondition.of(
      DatabaseComparison.create("owner", ownerId));
    return selectRows(condition).thenApply(rows ->
      rows.stream().map(ChatGPT::of).toList());
  }
}
