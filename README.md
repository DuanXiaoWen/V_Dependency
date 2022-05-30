# V-DependencyN

##  Env
- JDK:8~15
- Gradle
- IDEA 2021.2 and above


## Description

<!-- Plugin description -->
A code Dependency Visualization Plugin

This specific section is a source for the [plugin.xml](/src/main/resources/META-INF/plugin.xml) file which will be extracted by the [Gradle](/build.gradle.kts) during the build process.

To keep everything working, do not remove `<!-- ... -->` sections. 
<!-- Plugin description end -->

The software development process requires developers to build an understanding of the program, and the cost of software understanding is increasing as the dependencies between components in a software system become more complex.
In the field of code dependency analysis, many visualization tools have been developed, which mainly analyze control dependencies or data dependencies of programs, however, current data dependency analysis tools have the following problems.

- They do not distinguish the read/write relationship of methods to fields, and the obtained data dependencies are rather general.
    
- The presentation of dependencies is separated from the development environment, and no direct correspondence between them is established.
    
To solve the above problems, I designed and implemented the code dependency visualization IDEA plug-in V-Dependency, which uses JVM TI to capture the field read/write contexts at code runtime, distinguish the types of read/write accesses to fields by methods, and derive data dependencies with different degrees of closeness, and use the code structure provided by IntelliJ Platform Plugin SDK to The data dependencies are mapped to methods and fields in the source code using the code structure analysis interface provided by the IntelliJ Platform Plugin SDK, and the dependencies are finally integrated to generate an intuitive and clear presentation layer, establishing an organic combination of dependencies and source code.

## Pre-release builds

Please note that the data source required for this plugin needs to be generated manually by the user.
The graph generation steps are as follows.

- Writing test class for interested module.
- Compiling dynamic link libraries.
- Run the test class to get the dependency source data and put it in the root directory of 
the module where the test class is located.
- Register the tool window after running the plugin.
- Select the module in which the test class is located in the Tools window.
- Click the Run button to generate the code dependency graph.