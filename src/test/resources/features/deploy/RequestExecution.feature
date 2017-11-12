Feature: Request Execution
  As an API user
  I want to request the execution of an automation job for a runner process
  So that the required deployment can be delegated to the runner for execution

  Scenario: Request execution of deployment configuration
    Given the payload is well defined
    When the execution of the deployment configuration is requested
    Then the deployment process has been launched