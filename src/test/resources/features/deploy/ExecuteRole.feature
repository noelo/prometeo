Feature: Execute Role
  As an API user
  I want to request the execution of a single Ansible role
  So that the role can be executed against a target host

  Scenario: Execute a role against a host
    Given the payload for single role execution is defined
    When the execution of the role is requested
    Then the deployment process has been launched