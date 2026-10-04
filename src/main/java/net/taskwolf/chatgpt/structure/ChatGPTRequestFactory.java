package net.taskwolf.chatgpt.structure;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpClient;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class ChatGPTRequestFactory {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public ChatGPTRequest create(UUID chatGPTId) {
    return ChatGPTRequest.create(chatGPTDatabaseTable, httpClient, chatGPTId);
  }
}
