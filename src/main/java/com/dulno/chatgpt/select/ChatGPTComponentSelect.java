package com.dulno.chatgpt.select;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.workflow.component.input.InputComponentSelect;
import com.dulno.core.workflow.component.input.InputComponentSelectEntry;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(staticName = "create")
public class ChatGPTComponentSelect implements InputComponentSelect {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;

  @Override
  public CompletableFuture<List<InputComponentSelectEntry>> compile(
    User user, UUID target, Map<String, String> previousInputs
  ) {
    return chatGPTDatabaseTable.findChatGPTsOfOwner(target)
      .thenApply(chatGPTs -> chatGPTs.stream()
        .map(chatGPT -> InputComponentSelectEntry.create(chatGPT.id().toString(),
          chatGPT.accountName()))
        .toList());
  }
}