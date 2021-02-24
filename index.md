![Shuttle](shuttle_header.png)

# Shuttle
Shuttle provides a modern, guarded way to pass large Serializable objects with Intents or saving them in Bundle objects to avoid app crashes.

Often, businesses experience the adverse side effects of risks introduced in daily software engineering.   These adverse side effects include time and money spent on app crash investigation, app crash fixes, quality assurance testing, releasing hotfixes, and extra governance through code reviews.

Shuttle reduces the high-level of governance needed to catch Transaction Too Large Exception inducing code by:
1. storing the Serializable and passes an identifier for the Serializable
2. using a small-sized Bundle for binder transactions
2. avoids app crashes from Transaction Too Large Exceptions
3. enables retrieval of the stored Serializable at the destination.

Why keep spending more time and money on governance through code reviews?  Why not embrace the problem by providing a solution for it?

When envisioning, designing, and creating the architecture, quality attributes, and best practices were in mind. These attributes include but are not limited to usability, readability, recognizability, reusability, and maintainability.

## Background
The Shuttle framework takes its name from cargo transportation in the freight industry.  Moving and storage companies experience scenarios where large moving trucks cannot transport cargo the entire way to the destination (warehouses, houses, et cetera).  These scenarios might occur from road restrictions, trucks being overweight from large cargo, and more.   As a result, companies use small Shuttle vans to transport smaller cargo groups on multiple trips to deliver the entire shipment.

After the delivery is complete, employees remove the cargo remnants from the shuttle vans and trucks.  This clean up task is one of the last steps for the job.

The Shuttle framework takes its roots in these scenarios by:
creating a smaller cargo bundle object to use in successfully delivering the data to the destination
shuttling the corresponding large cargo to a warehouse and storing it for pickup
linking the smaller cargo with the larger cargo by an identifier
providing a single source of truth (Shuttle interface) to use for transporting cargo
providing convenience functions to remove cargo (automatically or on-demand)

## Getting Started
Refer to the documentation and demo app as a starting point.

### Recommended Usage
For end users wishing to include the Shuttle Framework in a project, the best way to get started is by using the Shuttle interface with the CargoShuttle object as the implementation.  This interface provides a single source of truth.

When retrieving the cargo transported with Shuttle, the object returned is a Channel of type ShuttleResult.   This type is a sealed class, with states named Loading, Success, and Error.   Shuttle uses this type to promote the usage of the Loading-Content-Error (LCE) pattern.

### Cleaning up after use
To remove persisted cargo data after it is not needed, convenience functions are available for use.

## Architecture
This project architecture provides both the solution and Solution Building Block (SBB) framework.

### Modules
The Solution Building Block (SBB) composes logical groupings in their respective modules.  These modules include the following:
1. **Framework:** This module contains the core, essential solution components to get started.
2. **Framework Integrations:** These modules contain the necessary bridging/interfaces to integrate different technologies into the framework module.
3. **Framework Integration Extensions:** These modules contain the necessary code to complete solutions with the Framework module via Framework Integration modules.
4. **Framework Addons:** These modules are useful solutions or Solution Building Blocks(SBBs) built on the Shuttle Framework.

#### Framework Integrations and Extensions
The design of the Shuttle Framework enables flexibility by not forcing particular technologies on consuming projects.  An example of this includes the persistence module that provides the interfaces needed for integrating extensions with the Shuttle Framework.   One such extension is the Room extension, enabling Room to be used to persist the cargo.

#### Framework Addons
The Shuttle Framework is a Solution Building Block (SBB).  The Addons intend to include solutions/SBBs built on top of the Shuttle Framework.
One example of an addon is the navigation module that enables programmatic usage of the Shuttle Framework with the Navigator from Google's Navigation Architecture Component.

### Technologies in use by the Shuttle Framework
The Shuttle Framework's design includes the avoidance of imposing technologies on consumers and packing large-sized transitive dependencies.  Often, Framework packaging includes reactive libraries, despite their large sizes. The Shuttle Framework uses Kotlin Coroutines to provide the asynchronous communication needed to meet the goals.

## Heads Up
If there is other data, like Parcelable objects included in on intent data, app crashes may still occur from Transaction Too Large exceptions.  Even though it is best practice to use Parcelable objects over Serializable objects, since objects are stored on disk and Google warns to not write Parcelable objects to disk, Serializable objects are used.

## The Demo App
The demo apps introduce the problem by showing one of the most common use cases is the transportation of image data in Serializables.

The demo apps includes usage of Shuttle with MVVM and MVC style architectures.

With MVVM, the activities and fragments are a part of the View component.   The ViewModel is the liaison between the View and the Model.   It maintains the state of the View. It may take actions from the View events and perform actions on the Model.  It may respond to events from the Model and manipulate the View.

In the demo app, the ViewModel component is using Google's ViewModel Architecture Component.  The asynchronous notification mechanism used in MVVM is provided by using Kotlin Channels, much like Google's Databinding library's Observables.

With MVC, the activities and fragments are a part of the Controller Component.  The controllers receive input and modify it for the models or views.

