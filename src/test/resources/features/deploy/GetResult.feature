Feature: Get Result of Execution
  As an API user
  I want to query the result of an execution
  So that I can decide if further action is required.

  Scenario: Get result
    When the result of the execution is requested
    Then the execution result is retrieved