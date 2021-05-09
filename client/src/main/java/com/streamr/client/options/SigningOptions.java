package com.streamr.client.options;

public class SigningOptions {
  private final SignatureVerificationPolicy verifySignatures;

  public enum SignatureVerificationPolicy {
    AUTO,
    ALWAYS
  }

  public SigningOptions(SignatureVerificationPolicy verifySignatures) {
    this.verifySignatures = verifySignatures;
  }

  public SignatureVerificationPolicy getVerifySignatures() {
    return verifySignatures;
  }
}
