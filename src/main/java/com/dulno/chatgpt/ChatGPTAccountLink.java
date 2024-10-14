package com.dulno.chatgpt;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.core.account.AccountLink;
import com.dulno.core.account.AccountLinkEntry;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public final class ChatGPTAccountLink implements AccountLink {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;

  @Override
  public CompletableFuture<Boolean> accountExists(UUID id) {
    return chatGPTDatabaseTable.chatGPTExists(id);
  }

  @Override
  public CompletableFuture<List<AccountLinkEntry>> findAccounts(UUID id) {
    return chatGPTDatabaseTable.findChatGPTsOfOwner(id)
      .thenApply(chatGPTs -> chatGPTs.stream()
        .map(chatGPT -> AccountLinkEntry.create(chatGPT.id().toString(),
          chatGPT.accountName()))
        .toList());
  }

  @Override
  public void removeAccount(UUID id, String identifier) {
    chatGPTDatabaseTable.deleteChatGPT(UUID.fromString(identifier));
  }

  @Override
  public String registrationUrl(UUID id, String apiKey) {
    return "https://dulno.com/chatgpt/connect/";
  }

  @Override
  public String description() {
    return "chatgpt.link.description";
  }
}