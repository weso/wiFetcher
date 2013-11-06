Feature: Cucumber
  In order to load correctly secondary observations data from Web Index spreadsheets
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from Web Index spreadsheets
    
  	Given I want to load the observations of dataset "ITU B-Imputed"
  	When I check the value for the country "Rwanda" and year "2009"
    Then the value should be "0.0214716414798364"
    
    Given I want to load the observations of dataset "RSF A-Imputed"
  	When I check the value for the country "Portugal" and year "2011"
    Then the value should be "5.33"
    