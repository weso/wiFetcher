Feature: Cucumber
  In order to load correctly countries data from CSV file
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from countries CSV File
    
  	Given I want to load names and iso-codes for all countries presents in WebIndex
  	When I check 
    Then the value should be "0.0"
    
    Given I want to load the observations of indicator "ITUA-IMPUTED"
  	When I check the value for the country "Afghanistan" in the year "2012"
    Then the value should be "5.0"
    
  	Given I want to load the observations of indicator "ITUA"
  	When I check the value for the country "Angola" in the year "2007"
    Then it should raise an Exception