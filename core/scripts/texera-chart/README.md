# Texera Chart

This directory contains the helm chart of deploying the Texera with user system from scratch on Kubernetes

## Prerequisites
- Kubernetes Version:
- Number of Nodes: 

## Getting Started

### Step 0: copy the ddl file to files directory

Use the below command to copy the ddl file to `texera-chart`
```shell
cd texera/core/scripts/texera-chart
cp ../sql/texera_ddl.sql files/
```
This step is needed in order to create the `texera_db` 

### Install the chart
Use `helm install`


