Feature: Cucumber
  In order to load correctly data from Web Index spreadsheets
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from Web Index spreadsheets
    
  	Given I want to load the observations of indicator "ITUA-Raw"
  	When I check the value for the country "Albania" in the year "2009"
    Then the value should be "0.0"
    
    Given I want to load the observations of indicator "ITUA-IMPUTED"
  	When I check the value for the country "Afghanistan" in the year "2012"
    Then the value should be "5.0"
    
  	Given I want to load the observations of non-existing indicator "ITUA"