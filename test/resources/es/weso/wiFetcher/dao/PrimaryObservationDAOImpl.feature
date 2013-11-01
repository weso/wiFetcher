Feature: Cucumber
  In order to load correctly primary observations data from Web Index spreadsheets
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from Web Index spreadsheets
    
  	Given I want to load the observations of primary indicators
  	When I check the value for the country "Armenia" and indicator "Q2"
    Then the value should be "2.0"
    
    Given I want to load the observations of primary indicators
  	When I check the value for the country "Finland" and indicator "Q1"
    Then the value should be "7.0"
    Then the value should not be "3.0"