# Changelog

## [Released]
## [2.0.1] - 2022-07-23
## Added
- the new back press callback for SDK version 33
- the new back press trigger for SDK version 33 to avoid the deprecated call to `activity.onBackPressed()`.

## Changed
- updated versions for many dependencies
- updated unit tests to remove redundant annotations
- updated unit tests to test the new back press handling

## Removed
- Some nullability handling that is no longer needed since the objects cannot be null

## [2.0.0] - 2022-03-27
## Added
- an instant task executor for unit tests
- an extension function for CompositeDisposableHandle
- manifests to fix an Android 12 issue with a dependency with manifests missing the exported flag

## Changed
- updated dependency versions including but not limited to Kotlin 1.6.0
- updated unit tests with changes for Coroutines with Kotlin 1.6.0
- updated code for Android 12 and Java 11
- updated the readme file

## Removed
- unused proguard files with modules that have consumer proguard files
- the run configuration file

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
