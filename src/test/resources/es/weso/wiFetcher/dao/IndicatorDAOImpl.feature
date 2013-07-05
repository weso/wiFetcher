Feature: Cucumber
  In order to load correctly indicators data from excel file
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from indicators excel File
    
  	Given I want to load all information about indicators in the WebIndex
  	When I check the indicator with "id" "FHA"
    Then the indicator "name" should be "Political rights"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "name" "Information on jobs"
    Then the indicator "id" should be "Q8c" 
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "name" "Information on jobs"  
    Then the indicator "id" should not be "FHA" 
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "description" "Survey Question: How free is the press in your country? [1 = totally restricted; 7 = completely free]"
    Then the indicator "id" should be "WEFC"  
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "id" "WIKIA"
    Then the indicator "type" should be "Secondary"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "id" "Q8a"
    Then the indicator "weight" should be "1"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "id" "RSFA"
    Then the indicator "source" should be "RSF"
    
    Given I want to load all information about indicators in the WebIndex
    When I check the indicator with "id" "ITUA"
    Then the indicator "hl" should be "High"