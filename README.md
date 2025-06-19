# legiscan

## Overview

Provides reusable Java utilities for connecting to and fetching data from a remote Legiscan service.

https://legiscan.com/legiscan

The primary offerings of this library are as follows:
1. [LegiscanService](https://github.com/rrowlands/legiscan/blob/main/src/main/java/us/poliscore/legiscan/service/LegiscanService.java) - A basic communication service which implements the full Legiscan HTTP API
2. [CachedLegiscanService](https://github.com/rrowlands/legiscan/blob/main/src/main/java/us/poliscore/legiscan/service/CachedLegiscanService.java) - A caching wrapper around the LegiscanService. Extends the default API with the 'cacheDataset' method.
3. [LegiscanClient](https://github.com/rrowlands/legiscan/blob/main/src/main/java/us/poliscore/legiscan/LegiscanClient.java) - A CLI accessor to the CachedLegiscanService and LegiscanService.
4. [us.poliscore.legiscan.view](https://github.com/rrowlands/legiscan/blob/main/src/main/java/us/poliscore/legiscan/view) - Java POJOs (Plain Old Java Object) to provide type-safe accessors to the JSON objects

A 'mvn install' on the root produces two separate jars:
1. legiscan-version.jar - A traditional Java jar for usage as a dependency in a Java project
2. legiscan-version-cli.jar - A 'fat Jar' built using the Maven Shade plugin, for use in a standalone CLI context. Accessible via Maven as a 'cli' classifier.

## cacheDataset

The 'cacheDataset' operation allows you to download, unzip, and store a Legiscan dataset to a local filesystem cache of your choosing. By default, this dataset is stored at:
`<user.home>/appdata/poliscore/legiscan`

The `--cache-dir` parameter allows you to change where the cache is stored.

cacheDataset is a combination of a few different Legiscan API methods. First, the operation invokes 'getDatasetRaw' to download the dataset in bulk. The response is then unzipped and loaded into the cache. Then, 'getMasterListRaw' is invoked and the 'change_hash' is checked for every bill in the dataset to ensure that the dataset is fully up-to-date. Out of date bills are updated with the 'getBill' operation. Finally, if 'cacheDataset' is run again at some point in the future, any previously fetched bills will have their cache TTL refreshed.

## Usage

### CLI

The cli jar provides for a command line interface, built using commons cli. The output of the help command is as follows:

```
usage: LegiscanClient
 -a,--accessKey <arg>     Access key for dataset retrieval
 -ac,--action <arg>       Action to take for setMonitor: monitor, remove,
                          or set
 -c,--no-cache            Disable caching (enabled by default)
 -cd,--cache-dir <arg>    Directory to use for cached data. (default:
                          <user.home>/appdata/poliscore/legiscan)
 -ct,--cache-ttl <arg>    Time to live for cached items in seconds
                          (default: 14400)
 -f,--format <arg>        Format for dataset (json, csv)
 -i,--id <arg>            ID for operations requiring a
                          bill/session/person ID
 -k,--key <arg>           LegiScan API key
 -m,--monitor-ids <arg>   Comma-separated list of bill IDs to monitor
                          (required for setMonitor)
 -op,--operation <arg>    Operation to perform. Valid values:
                          cacheDataset, getBill, getBillText,
                          getAmendment,
                          getSupplement, getRollCall, getPerson,
                          getSessionList, getMasterList,
                          getMasterListRaw, getSearch, getSearchRaw,
                          getDatasetList, getDataset,
                          getDatasetRaw, getSessionPeople,
                          getSponsoredList, getMonitorList,
                          getMonitorListRaw, setMonitor
 -p,--page <arg>          Page number for paginated search
 -q,--query <arg>         Query string for search
 -r,--record <arg>        Record filter for monitor list (current,
                          archived, year)
 -s,--state <arg>         State abbreviation (e.g., CA, TX)
 -sp,--special            Special. Used for cacheDataset. (default: false)
 -st,--stance <arg>       Stance to apply (optional, defaults to 'watch')
 -y,--year <arg>          Year filter (e.g., 2024)
```

Here's a few examples:

```
# Download the poliscore legiscan 'far jar' from maven central
mvn dependency:copy -Dartifact=us.poliscore:legiscan:1.0.0:jar:cli -DoutputDirectory=.

# Cache a dataset
java -jar legiscan-1.0.0-cli.jar --key 123 -op cacheDataset --state US --year 2020

# Manually fetch a bill
java -jar legiscan-1.0.0-cli.jar --key 123 -op getBill --id 2028513
```

Just replace '--key 123' with your legiscan key. This library has been developed and tested on Java 21.


### Maven Dependency

This library has been published to Maven central and can be added as a library dependency to a pom as follows:

```
<dependency>
    <groupId>us.poliscore</groupId>
    <artifactId>legiscan</artifactId>
    <version>1.0.0</version>
</dependency>
```

This is especially useful for leveraging the built-in [us.poliscore.legiscan.view](https://github.com/rrowlands/legiscan/blob/main/src/main/java/us/poliscore/legiscan/view) POJOs for type-safe usecases.

## About the Author

This library is provided free of charge under MIT license as part of the larger mission of PoliScore - Making legislation more understandable and accessible.

https://poliscore.us/about
