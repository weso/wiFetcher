Feature: Cucumber
  In order to load correctly subindexes data from Web Index spreadsheets
  As a machine
  I want to be able to validate the data

  Scenario: Validate the subindexes data loading from Web Index spreadsheets
    
  	Given I want to load all information about subindexes in the WebIndex
  	When I check all subindexes and components are loaded
    Then There are "7" subindexes and "11" components
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the subindex with "name" "Impact"
    Then the subindex "id" should be "Impact"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the subindex with "id" "Readiness"
    Then the subindex "weight" should be "0.2"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the subindex with "id" "TheWeb"
    Then the subindex "description" should be "This sub-index assesses the availability of relevant and useful content, as well as the number of Internet and Web users in a country"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the numbers of components of subindex "TheWeb"
    Then the number of components should be "2"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "id" "Social"
    Then the component "name" should be "Social Impact"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "name" "Communications Infrastructure"
    Then the component "id" should be "CommunicationsInfrastructure"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "id" "WebUse"
    Then the component "weight" should be "0.5"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the component with "id" "Political"
    Then the component "description" should be "This component assesses the utility of the Web and its impact on politics and government"
    
    Given I want to load all information about subindexes in the WebIndex
    When I check the number of indicators of component "WebUse"
    Then the number of indicators should be "7"