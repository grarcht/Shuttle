# Changelog

## [Released]

## [3.0.0] - 2024-05-21

## Added

- framework code to support Android services (remote and local): ```ShuttleService```, 
  ```LifecycleAwareShuttleServiceConnection```, ```ShuttleServiceMessageValidator```, and more.
- mvvm-with-a-service demo module
- a cargo id parameter to the new states from version 2.1.0
- a test class for the message validator

## Changed

- reverted an artifact versions to get the compose code to build
- brought back some databinding temporarily for the demos with the intention to remove it soon
- updated the documentation with Dokka
- updated the compile and target sdk version to 34
- code updates for issues reported by detekt
- organized import statements
- dependency declaration clean up


## [2.1.0] - 2023-08-29

## Added

- new states for the uninitialized
  states: ```ShuttlePickupCargoResult.NotPickingUpCargoYet```,```ShuttleRemoveCargoResult.NotRemovingCargoYet```,
  and ``` ShuttleStoreCargoResult.NotStoringCargoYet``` to help solutions using ```StateFlows``` to
  have initial states for picking up, removing, or storing cargo data.

## Changed

- Gradle to version 8.3
- other dependency artifact versions to newer versions
- Java versions for kotlin, source, etc, to target 17
- updated the compile and target sdk property names
- updated the compile and target sdk property versions
- updated the documentation with Dokka
- maven repository url ordering
- updated tests to reflect the state additions
- updated some of the scopes from MainScope to lifecycle and viewmodel
- R ids for incremental compilation speed improvements
- ```Flow``` usage in demos to use ```StateFlow```.

## Removed

- databinding and the associated project module
- Dokka configuration for previous Dokka versions

## [2.0.4] - 2023-02-26

## Added

- suppression annotations for the ```SwallowedException``` warnings
- replacements for activity attributes to the Framework module's manifest file to remove warnings

## Changed

- updated versions for many dependencies
- updated the ```serialVersionUID``` in the ```Serializable``` classes
- refactored the manifest package declaration to the namespace in the build.gradle files
- statements throwing ```IllegalStateException``` to use ```error(message)```

## [2.0.3] - 2022-10-21

## Changed

- dependency versions
- shuttle version references from build scripts

## Removed

- an unnecessary unit test assertion

## [2.0.2] - 2022-07-24

## Added

- manifest opt-in for the new back press callback for SDK 33

## Changed

- rolled back dependency versions to stable version

## Removed

- the aar file classifier

## [2.0.1] - 2022-07-23 - DON'T USE...USE 2.0.2 instead

## Added

- the new back press callback for SDK version 33
- the new back press trigger for SDK version 33 to avoid the deprecated call
  to `activity.onBackPressed()`.

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

- added unit tests in the framework, framework-integrations-persistence, and
  framework-integrations-extensions-room modules
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
