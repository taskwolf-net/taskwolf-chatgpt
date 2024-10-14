package com.dulno.chatgpt.access;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTRequestFactory;
import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.access.DulnoRestController;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
public class ChatGPTController extends DulnoRestController {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final UserTargetDatabaseTable userTargetDatabaseTable;
  private final TeamTargetDatabaseTable teamTargetDatabaseTable;
  private final ChatGPTRequestFactory chatGPTRequestFactory;

  private ChatGPTController(
    Key productKey, UserDatabaseTable userDatabaseTable,
    ChatGPTDatabaseTable chatGPTDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable,
    ChatGPTRequestFactory chatGPTRequestFactory
  ) {
    super(productKey, userDatabaseTable);
    this.chatGPTDatabaseTable = chatGPTDatabaseTable;
    this.userTargetDatabaseTable = userTargetDatabaseTable;
    this.teamTargetDatabaseTable = teamTargetDatabaseTable;
    this.chatGPTRequestFactory = chatGPTRequestFactory;
  }

  @RequestMapping(path = "/chatgpt/add/", method = RequestMethod.POST)
  public void addChatGPT(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    findUser(request)
      .thenAccept(user -> userTargetDatabaseTable.findTargetSecured(user.id())
        .thenAccept(target -> findChatGPTOwner(user, target)
          .thenAccept(owner -> chatGPTDatabaseTable.generateAvailableChatGPTId()
            .thenAccept(id -> addChatGPT(id, owner, body.getString("accessToken"),
              body.getString("organization"))))));
  }

  private CompletableFuture<UUID> findChatGPTOwner(User user, UUID target) {
    return user.id().equals(target) ?
      CompletableFuture.completedFuture(target) :
      teamTargetDatabaseTable.findTargetSecured(user.id())
        .thenApply(team -> team.orElse(target));
  }

  private void addChatGPT(
    UUID id, UUID ownerId, String accessToken, String organizationId
  ) {
    //TODO: FIND CORRECT CHATGPT ACCOUNT NAME
    chatGPTDatabaseTable.insertChatGPT(id, ownerId, "", accessToken,
      organizationId);
  }
}
