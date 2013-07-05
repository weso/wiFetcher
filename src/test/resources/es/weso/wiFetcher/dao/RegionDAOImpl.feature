Feature: Cucumber
  In order to load correctly regions data from excel file
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from regions excel File
    
  	Given I want to load all information about regions
  	When I check the numbers of regions
  	Then the number of regions should be "5"
  	
  	Given I want to load all information about regions
  	When I check the region with the name "Asia pacific"
    Then the region should have "14" countries
    
    Given I want to load all information about regions
    When I check the region with the name "Africa"
    Then the region should have the country "Cameroon"
    
    Given I want to load all information about regions
    When I check the region with the name "Americas"
    Then the region should not have the country "France"