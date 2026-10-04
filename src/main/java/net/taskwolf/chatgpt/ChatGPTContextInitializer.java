package net.taskwolf.chatgpt;

import net.taskwolf.chatgpt.structure.ChatGPTDatabaseTable;
import net.taskwolf.chatgpt.structure.ChatGPTRequestFactory;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
@RequiredArgsConstructor(staticName = "create")
public final class ChatGPTContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final ChatGPTDatabaseTable chatGPTDatabaseTable;
  private final ChatGPTRequestFactory chatGPTRequestFactory;

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("chatGPTDatabaseTable", chatGPTDatabaseTable);
    beanFactory.registerSingleton("chatGPTRequestFactory", chatGPTRequestFactory);
  }
}
