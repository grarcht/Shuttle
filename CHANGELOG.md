# Changelog

## [Released]
## [1.0.0] - 2021-07-01
## Added
- added unit tests for the navigation framework addon
- added a new context modeling diagram

### Changed
- updated unit tests with corrections for detekt errors/warnings
- updated the module relationships modeling diagram
- updated dependency versions
- made google repository maven url adjustments to enable Hilt version 2.37 to work with the project
- updated the readme file

## [1.0.0-beta01] - 2021-06-16
### Added
- added unit tests in the framework, framework-integrations-persistence, and framework-integrations-extensions-room modules
- added an early return and channel send to ShuttleRepository

### Changed
- set the compileSDKVersion to 30
- project dependency versions
- fixed unit tests in ShuttleBundleTest
- corrected the file delete function calls in ShuttlePersistenceFileSystemGateway
- updated code in ShuttleDataWarehouse (the warehouse used for unit testing)
- changed ShuttleCargoFacade for unit testing
- updated the code documentation

## [1.0.0-alpha01] - 2021-2-23
- Initial changes to shuttle cargo
