Feature: Cucumber
  In order to load correctly subindexes data from Web Index spreadsheets
  As a machine
  I want to be able to validate the data

  Scenario: Validate the subindexes data loading from Web Index spreadsheets
    
  	Given I want to load all information about subindexes in the WebIndex
  	When I check all subindexes and components are loaded
    Then There are "4" subindexes and "10" components
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the subindex with "name" "Impact and Empowerment"
    Then the subindex "id" should be "empowerment"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the subindex with "id" "content"
    Then the subindex "weight" should be "0.2"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the subindex with "id" "access"
    Then the subindex "description" should be ""
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the numbers of components of subindex "freeopen"
    Then the number of components should be "2"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "id" "economic"
    Then the component "name" should be "Economic impact"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "name" "Access and Affordability"
    Then the component "id" should be "affordability"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "id" "creation"
    Then the component "weight" should be "0.5"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "id" "political"
    Then the component "description" should be ""