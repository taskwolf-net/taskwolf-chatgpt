package net.taskwolf.chatgpt;

import net.taskwolf.chatgpt.action.prompt.ChatGPTPromptAction;
import net.taskwolf.chatgpt.select.ChatGPTComponentSelect;
import net.taskwolf.chatgpt.select.ModelComponentSelect;
import net.taskwolf.chatgpt.structure.ChatGPTDatabaseTable;
import net.taskwolf.chatgpt.structure.ChatGPTRequestFactory;
import net.taskwolf.core.account.AccountLink;
import net.taskwolf.workflow.action.ActionRepository;
import net.taskwolf.core.database.DatabaseConnection;
import net.taskwolf.core.database.DatabaseKeyspace;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.module.ModuleDescription;
import net.taskwolf.core.module.ModuleInformation;
import net.taskwolf.core.module.ModuleLoadPriority;
import net.taskwolf.workflow.integration.Integration;
import net.taskwolf.workflow.trigger.TriggerRepository;
import net.taskwolf.workflow.component.input.InputComponentSelect;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import org.springframework.boot.SpringApplication;

@ModuleDescription(name = "chatgpt", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class ChatGPTModule extends Integration {
  private Log log;
  private SpringApplication springApplication;
  private ChatGPTContextInitializer contextInitializer;
  private AccountLink accountLink;
  private InputComponentSelect chatGPTComponentSelect;
  private InputComponentSelect modelComponentSelect;


  public ChatGPTModule(Injector injector) {
    super(injector.createChildInjector(ChatGPTInjectionModule.create()));
  }

  @Override
  public void enable() throws Exception {
    log = injector().getInstance(Log.class).subLog("ChatGPT");
    springApplication = injector().getInstance(SpringApplication.class);
    var chatGPTDatabaseTable = injector().getInstance(ChatGPTDatabaseTable.class);
    var chatGPTRequestFactory = injector().getInstance(ChatGPTRequestFactory.class);
    contextInitializer = ChatGPTContextInitializer.create(chatGPTDatabaseTable,
      chatGPTRequestFactory);
    springApplication.addInitializers(contextInitializer);
    accountLink = ChatGPTAccountLink.create(chatGPTDatabaseTable);
    chatGPTComponentSelect = ChatGPTComponentSelect.create(chatGPTDatabaseTable);
    modelComponentSelect = ModelComponentSelect.create(chatGPTDatabaseTable,
      chatGPTRequestFactory);
  }

  @Override
  public void disable() {
    var initializers = Lists.newArrayList(springApplication.getInitializers());
    initializers.remove(contextInitializer);
    springApplication.setInitializers(initializers);
  }

  @Override
  public AccountLink accountLink() {
    return accountLink;
  }

  @Override
  public ModuleInformation moduleInformation() {
    return ModuleInformation.create("ChatGPT", "", "chatgpt",
      ModuleInformation.Type.PUBLIC);
  }

  @Override
  public TriggerRepository triggerRepository() {
    return TriggerRepository.create();
  }

  @Override
  public ActionRepository actionRepository() {
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var chatGPTDatabaseTable = injector().getInstance(ChatGPTDatabaseTable.class);
    var chatGPTRequestFactory = injector().getInstance(ChatGPTRequestFactory.class);
    var repository = ActionRepository.create();
    repository.registerAction(ChatGPTPromptAction.create(chatGPTComponentSelect,
      modelComponentSelect, chatGPTDatabaseTable, chatGPTRequestFactory,
      databaseConnection, databaseKeyspace));
    return repository;
  }
}