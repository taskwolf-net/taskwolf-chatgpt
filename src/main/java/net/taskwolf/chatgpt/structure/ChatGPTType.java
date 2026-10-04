package net.taskwolf.chatgpt.structure;

public enum ChatGPTType {
  PERSONAL,
  ORGANIZATION;

  public boolean isPersonal() {
    return this == PERSONAL;
  }

  public boolean isOrganization() {
    return this == ORGANIZATION;
  }
}
