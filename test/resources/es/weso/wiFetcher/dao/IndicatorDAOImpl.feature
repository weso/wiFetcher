Feature: Cucumber
  In order to load correctly indicators data from excel file
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from indicators excel File
    
  	Given I want to load all information about indicators in the WebIndex
  	When I check the indicator with "id" "ITU K"
    Then the indicator "type" should be "Secondary"
    Then the number of primary indicators should be "53"
    Then the number of secondary indicators should be "38"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "name" "Civil Liberties"
    Then the indicator "id" should be "FH B" 
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "name" "Enforcement of web accessibility by people with disability"  
    Then the indicator "id" should not be "Q19" 
    
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
    When I check the indicator with "id" "ODB.I.2"
    Then the indicator "hl" should be "High"