package com.dulno.chatgpt;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "create")
public class ChatGPTInjectionModule extends AbstractModule {
  @Override
  protected void configure() {

  }

  @Provides
  @Singleton
  ChatGPTDatabaseTable provideChatGPTDatabaseTable(
    DatabaseConnection connection, DatabaseKeyspace keyspace
  ) {
    var chatGPTDatabaseTable = ChatGPTDatabaseTable.create(connection, keyspace);
    chatGPTDatabaseTable.createIfNotExists();
    chatGPTDatabaseTable.createIndexIfNotExists("owner");
    return chatGPTDatabaseTable;
  }
}
