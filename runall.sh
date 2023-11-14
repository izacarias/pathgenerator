#!/usr/bin/bash
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 10000  -r 5 -t URLLC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 20000  -r 5 -t URLLC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 30000  -r 5 -t URLLC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 40000  -r 5 -t URLLC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 50000 -r 5 -t URLLC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 10000 -r 5 -t eMBB
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 20000 -r 5 -t eMBB
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 30000 -r 5 -t eMBB
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 40000 -r 5 -t eMBB
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 50000 -r 5 -t eMBB
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 10000 -r 5 -t mMTC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 20000 -r 5 -t mMTC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 30000 -r 5 -t mMTC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 40000 -r 5 -t mMTC
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 50000 -r 5 -t mMTC
# Mixed
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 10000 -r 5
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 20000 -r 5
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 30000 -r 5
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 40000 -r 5
java -jar target/placement-1.0-SNAPSHOT-runnable.jar demands -l 50000 -r 5
