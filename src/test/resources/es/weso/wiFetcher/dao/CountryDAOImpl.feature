Feature: Cucumber
  In order to load correctly countries data from CSV file
  As a machine
  I want to be able to validate the data

  Scenario: Validate the data loading from countries CSV File
    
  	Given I want to load names and iso-codes for all countries presents in WebIndex
  	When I check the country with the "name" "New Zealand"
    Then the "iso2-code" should be "NZ"
    
    Given I want to load names and iso-codes for all countries presents in WebIndex
  	When I check the country with the "name" "United Kingdom of Great Britain and Northern Ireland"
    Then the "iso3-code" should be "GBR"
    
  	Given I want to load names and iso-codes for all countries presents in WebIndex
  	When I check the country with the "iso2-code" "AN"
    Then the "name" should be "Netherlands Antilles"
    
    Given I want to load names and iso-codes for all countries presents in WebIndex
    When I check the country with the "iso2-code" "YT"
    Then the "iso3-code" should be "MYT"
    
    Given I want to load names and iso-codes for all countries presents in WebIndex
    When I check the country with the "iso3-code" "LUX"
    Then the "iso2-code" should be "LU"
    
    Given I want to load names and iso-codes for all countries presents in WebIndex
    When I check the country with the "iso3-code" "VEN"
    Then the "name" should be "Venezuela (Bolivarian Republic of)"
    
    Given I want to load names and iso-codes for all countries presents in WebIndex
    When I check the country with the "iso3-code" "XXX"
    Then the result should be null
    
    Given I want to load names and iso-codes for all countries presents in WebIndex
    When I check the country with the "iso2-code" "XX"
    Then the result should be null
    
    Given I want to load names and iso-codes for all countries presents in WebIndex
    When I check the country with the "name" "xxxxxxxx"
    Then the result should be null