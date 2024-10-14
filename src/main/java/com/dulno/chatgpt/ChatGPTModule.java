package com.dulno.chatgpt;

import com.dulno.chatgpt.structure.ChatGPTDatabaseTable;
import com.dulno.chatgpt.structure.ChatGPTRequestFactory;
import com.dulno.core.account.AccountLink;
import com.dulno.core.action.ActionRepository;
import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.database.DatabaseKeyspace;
import com.dulno.core.log.Log;
import com.dulno.core.module.Module;
import com.dulno.core.module.ModuleDescription;
import com.dulno.core.module.ModuleInformation;
import com.dulno.core.module.ModuleLoadPriority;
import com.dulno.core.trigger.TriggerRepository;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import org.springframework.boot.SpringApplication;

@ModuleDescription(name = "chatgpt", version = "1.0.0-SNAPSHOT",
  priority = ModuleLoadPriority.NEUTRAL)
public final class ChatGPTModule extends Module {
  private Log log;
  private SpringApplication springApplication;
  private ChatGPTContextInitializer contextInitializer;
  private AccountLink accountLink;

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
    var databaseConnection = injector().getInstance(DatabaseConnection.class);
    var databaseKeyspace = injector().getInstance(DatabaseKeyspace.class);
    var repository = TriggerRepository.create();
    return repository;
  }

  @Override
  public ActionRepository actionRepository() {
    return ActionRepository.create();
  }
}