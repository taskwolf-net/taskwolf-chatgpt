package net.taskwolf.chatgpt.structure;

import net.taskwolf.core.database.DatabaseRow;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class ChatGPT {
  public static ChatGPT of(DatabaseRow row) {
    return create(row.findCell(0).uuidValue(), row.findCell(1).uuidValue(),
      row.findCell(2).stringValue(),
      ChatGPTType.valueOf(row.findCell(3).stringValue()),
      row.findCell(4).stringValue(), row.findCell(5).stringValue());
  }

  private final UUID id;
  private final UUID ownerId;
  private final String accountName;
  private final ChatGPTType accountType;
  private final String accessToken;
  private final String organizationId;
}
