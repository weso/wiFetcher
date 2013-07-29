Feature: Cucumber
  In order to load correctly data from Web Index spreadsheets
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from Web Index spreadsheets
    
  	Given I want to load the observations of dataset "2009" in the year "2009"
  	When I check the value for the country "Rwanda" and indicator "UNA"
    Then the value should be "10.7"
    
    Given I want to load the observations of dataset "2011" in the year "2011"
  	When I check the value for the country "Portugal" and indicator "ITUA"
    Then the value should be "129568.308303689"
    
  	Given I want to load the observations of non-existing dataset "ITUA" in the year "2009"