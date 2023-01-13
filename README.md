# Pacement of O-RAN Functional Blocks
Repository to store initial programming on the placement of CU and DU functional blocks on O-RAN architecture.

## Requirements
- Apache Maven (>= 3.8.6)
- Oracle(r) Java Development Kit (>= 17.0.2)

## Dependencies

1. Install Gurobi Linear Optimizar as the instructions in the official website
2. Include gurobi.jar to your local maven repository (aka. `~/.m2` directory)
```bash
$ mvn install:install-file -Dfile=/opt/gurobi952/linux64/lib/gurobi.jar -DgroupId=gurobi -DartifactId=gurobi -Dversion=9.5.2 -Dpackaging=jar
```

## Build the project
```
$ mvn clean compile package
```

## Run the application
```
$ java -jar target/placement-0.0.1-SNAPSHOT.jar
```
