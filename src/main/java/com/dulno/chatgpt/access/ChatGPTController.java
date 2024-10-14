package com.dulno.chatgpt.access;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTType;
import com.dulno.core.access.DulnoRequestBody;
import com.dulno.core.access.DulnoRestController;
import com.dulno.core.organization.team.TeamTargetDatabaseTable;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.user.UserTargetDatabaseTable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@RestController
public class ChatGPTController extends DulnoRestController {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final UserTargetDatabaseTable userTargetDatabaseTable;
  private final TeamTargetDatabaseTable teamTargetDatabaseTable;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  private ChatGPTController(
    Key productKey, UserDatabaseTable userDatabaseTable,
    ChatGPTDatabaseTable chatGPTDatabaseTable,
    UserTargetDatabaseTable userTargetDatabaseTable,
    TeamTargetDatabaseTable teamTargetDatabaseTable
  ) {
    super(productKey, userDatabaseTable);
    this.chatGPTDatabaseTable = chatGPTDatabaseTable;
    this.userTargetDatabaseTable = userTargetDatabaseTable;
    this.teamTargetDatabaseTable = teamTargetDatabaseTable;
  }

  @RequestMapping(path = "/chatgpt/add/", method = RequestMethod.POST)
  public CompletableFuture<Map<String, Object>> addChatGPT(
    HttpServletRequest request, @RequestBody String payload,
    HttpServletResponse response
  ) {
    var body = DulnoRequestBody.of(payload, response);
    var accessToken = body.getString("accessToken");
    var organizationId = body.getString("organization");
    return findUser(request)
      .thenCompose(user -> userTargetDatabaseTable.findTargetSecured(user.id())
        .thenCompose(target -> findChatGPTOwner(user, target)
          .thenCompose(owner -> chatGPTDatabaseTable.generateAvailableChatGPTId()
            .thenCompose(id -> findOrganizations(accessToken)
              .thenApply(organization -> addChatGPT(id, owner, accessToken,
                organizationId, organization))))));
  }

  private CompletableFuture<UUID> findChatGPTOwner(User user, UUID target) {
    return user.id().equals(target) ?
      CompletableFuture.completedFuture(target) :
      teamTargetDatabaseTable.findTargetSecured(user.id())
        .thenApply(team -> team.orElse(target));
  }

  private Map<String, Object> addChatGPT(
    UUID id, UUID ownerId, String accessToken, String organizationId,
    HttpResponse<String> organizationResponse
  ) {
    if (organizationResponse.statusCode() != 200) {
      return Map.of("success", false);
    }
    var organizations = new JSONObject(organizationResponse.body())
      .getJSONArray("data");
    var organizationOptional = searchSelectedOrganization(organizations,
      organizationId);
    if (organizationOptional.isEmpty()) {
      return Map.of("success", false);
    }
    var organization = organizationOptional.get();
    var type = organization.getBoolean("personal") ? ChatGPTType.PERSONAL :
      ChatGPTType.ORGANIZATION;
    chatGPTDatabaseTable.insertChatGPT(id, ownerId,
      findAccountName(organization, type), type, accessToken, organizationId);
    return Map.of("success", true);
  }

  private static final Pattern EMAIL_EXTRACTION_PATTERN =
    Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

  private String findAccountName(JSONObject organization, ChatGPTType type) {
    if (type.isOrganization()) {
      return organization.getString("title");
    }
    var matcher = EMAIL_EXTRACTION_PATTERN.matcher(
      organization.getString("description"));
    if (matcher.find()) {
      return matcher.group();
    }
    return organization.getString("title");
  }

  private Optional<JSONObject> searchSelectedOrganization(
    JSONArray organizations, String organizationId
  ) {
    for (var i = 0; i < organizations.length(); i++) {
      var entry = organizations.getJSONObject(i);
      if ((organizationId.isEmpty() && entry.getBoolean("is_default")) ||
        (!organizationId.isEmpty() && entry.getString("id").equals(organizationId))
      ) {
        return Optional.of(entry);
      }
    }
    return Optional.empty();
  }

  private CompletableFuture<HttpResponse<String>> findOrganizations(
    String accessToken
  ) {
    var request = HttpRequest.newBuilder()
      .uri(URI.create("https://api.openai.com/v1/organizations"))
      .GET().header("Authorization", "Bearer " + accessToken).build();
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }
}
