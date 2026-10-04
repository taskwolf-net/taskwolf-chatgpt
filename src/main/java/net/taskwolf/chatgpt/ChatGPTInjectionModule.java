package net.taskwolf.chatgpt;

import net.taskwolf.chatgpt.structure.ChatGPTDatabaseTable;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
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
