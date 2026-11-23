package com.ventixe.identity.exception;

public class AccountUnavailableException extends RuntimeException {

  public AccountUnavailableException() {
    super("This account is not available");
  }
}
