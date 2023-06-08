# Path generator for nsp4j-optimizer project

## Requirements
- Apache Maven (>= 3.8.6)
- Oracle(r) Java Development Kit (>= 17.0.2)

## Build the project
```
$ mvn clean compile package
```

## Run the application
```
$ java -jar target/placement-1.0-SNAPSHOT-runnable.jar paths -t <topology_file.dgs> -k <number of shortest paths> -b <origin of paths> -e <destination of paths>

```
#### Where:
- `paths` indicates that the program should generate paths
- `<topology_file.dgs>` should be replaced by the complete paths of the .dgs file
- `<number of shortest paths>` is the number of k shortest paths to generate (e.g., `-k 3` will generate 3 shortest paths)
- `-b` indicates the tier of nodes that should be taken as the origin of the paths
- `-e` indicates the tier of nodes that should be taken as the destination of the paths

#### Example:
```
java -jar target/placement-1.0-SNAPSHOT-runnable.jar paths -t /home/zaca/devel/nsp4j-optimizer/src/main/resources/scenarios/dim36.dgs -k 3 -b 4 -e 0
```

#### Format of the .dgs file
The nodes should include the attribute `tier` in the .dgs file. For example:
```
DGS004
test 0 0
...
an 1 x:0.00359 y:0.00036 num_servers_node:10 server_capacity:3200000 tier:0
...
```