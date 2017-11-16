Feature: Get Public Key
  As an API user
  I want to retrieve the public key used by Prometeo to connect to a managed host
  So that I can deploy the key on the managed host.

  Scenario: Get key
    When the public key is requested
    Then the public key is retrieved