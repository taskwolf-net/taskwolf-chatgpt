package net.taskwolf.chatgpt.select;

import net.taskwolf.chatgpt.structure.ChatGPTDatabaseTable;
import net.taskwolf.core.user.User;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import net.taskwolf.workflow.component.input.InputComponentSelectEntry;
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