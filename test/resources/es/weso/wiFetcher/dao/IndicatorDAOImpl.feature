Feature: Cucumber
  In order to load correctly indicators data from excel file
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from indicators excel File
    
  	Given I want to load all information about indicators in the WebIndex
  	When I check the indicator with "id" "ITU K"
    Then the indicator "type" should be "Secondary"
    Then the number of primary indicators should be "57"
    Then the number of secondary indicators should be "34"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "name" "Civil Liberties"
    Then the indicator "id" should be "FH B" 
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "name" "IT Community Prioritisation of web Accessibility"  
    Then the indicator "id" should not be "Q20" 
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "description" "Technicians in R&D (per million population)"
    Then the indicator "id" should be "WB D"  
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "id" "Q11"
    Then the indicator "type" should be "Primary"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "id" "RSF A"
    Then the indicator "weight" should be "0.5"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "id" "ODB.2013.I.ECON"
    Then the indicator "hl" should be "High"