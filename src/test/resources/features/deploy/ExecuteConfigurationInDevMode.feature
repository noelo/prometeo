Feature: Execute configuration in Developer mode
  As a Developer
  I want to request the execution of an automation job for a runner process in developer mode
  So that I can test the API in my local development machine

  Scenario: Request execution of deployment configuration
    Given the working directory environment variable is set
    Given the payload for dev mode is well defined
    When the execution of the deployment configuration is requested
    Then the deployment process has been launched