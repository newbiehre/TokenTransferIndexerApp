# Token Transfer Indexer Application

## Description

### What it does
This application tracks and indexes ERC-721 Token Transfer events on the Ethereum network. It allow users to query all 
indexed ERC-721 token transfer events that occured while the application is running and enables users to check the connection status
and manually retry establishing a connection when connnection fails.

### Main technologies used
#### General
- Kotlin (version 1.9.23)
- Spring Boot (versiion 3.2.5)
- Gradle (version 7.4)
- PostgrSQL

#### Blockchain related
- Infura, for reliable websocket endpoint to EVM chain (https://www.infura.io/)
- Web3j, a library compatible with JVM languages for interacting with  ethereum client for EVM languages (https://docs.web3j.io/4.11.0/).

## Table of Contents
- [Getting started](#getting-started)
- [Tests](#tests)
- [Future work](#future-work)
- [Author](#author)
- [License](#license)

# Getting started

### Build project
Please ensure you are using the following:
- JDK version 21.0.2
```
openjdk 21.0.2 2024-01-16
OpenJDK Runtime Environment (build 21.0.2+13-58)
OpenJDK 64-Bit Server VM (build 21.0.2+13-58, mixed mode, sharing)
```
Then click on the build gradke button on IDE (or run terminal command `./gradlew build` to download all dependencies and compile the code.

#### 1. Open a Infura Account and obtain an API key
1. Login to Infura (https://www.infura.io/).
2. You should be taken to the API Keys page after login.
3. Create a new API key or select an existing one.
4. Ensure the following is checked and saved:
   - Endpoint networks ethereum: mainnet
   - ![image](https://github.com/newbiehre/TokenTransferIndexerApp/assets/58487237/ecc3ef27-466a-49da-b68e-0d530b6292cf)
5. Copy API key and paste it in infura.apiKey in application.yaml file (root path: src/main/resources/application.yaml)
   - Note this is required for running both the application and for Erc721UtilsTest.kt (as it makes a real API call using this API key).
    
#### 2. Have a local PostgreSQL database instance running
Please ensure you have a local postgreSQL server running with a database created.

1. Provide the following value for the application.yaml file (root path: src/main/resources/application.yaml):
  - Valid username.
  - Valid password.
  - Valid database. 
    - Replace the bolded word with a valid database name in datasource.url: `jdbc:postgresql://localhost:5432/**postgres**`

### 3. Run application
1. Find TokenTransferIndexerApplication.kt (root path: src/main/kotlin/com/example/demo/TokenTransferIndexerApplication.kt)
2. Left click on file and select 'Run 'TokenTransferIndexer'

## Tests
All test files are in src/test directory.

### Technologies used
- Mockito-kotlin (version 3.2.0)
- Mockk (versiion 3.2.5)
- Testcontainers (version 1.19.8)
- Testcontainers:postgresql (version 1.19.8)

### Set up
For running integration tests involving database testing (Tests: Erc721TokenDaoTest.kt, Erc721TOkenRepositoryTest.kt, Erc721UtilsTest.kt):
1. Open application-test.yaml file (root path: src/test/resources/application-test.yaml)
2. Provide a valid username and password to your PostgreSQL database (can be the same as your application.yaml values).
3. Install Docker Desktop and ensure a Docker instance is running (installation guide: https://docs.docker.com/desktop/).

For Erc721UtilsTest.kt:
1. It utilizes application.yaml file (root path: src/main/resources/application.yaml) to retreive infura.apiKey.
Ensure a valid api key value is provided here before running this test.

### Warning
The following test doesn't consistently pass Erc721SubscriptionServiceTest's Batch Processing test 
"Should not need to verify contract address (call checkIfErc721())) if found in database" (root path: src/test/kotlin/com/example/demo/services/subscription/Erc721SubscriptionServiceTest.kt).
Run it again and it should pass. Apologies for the inconvenience for the time being!

### Run tests
To run all tests, left click on src/test directory and select "Run tests in demo tests" using your IDE. 
To run individual tests instead, left click on a test file and select "Run..." using your IDE. 

## Future work
### Add more features
- Implement support for indexing ERC721 metadata especially those on IPFS, which would be relevant and valuable to users querying ERC-721 token events.
- Expand monitoring and indexing of other token transfers that occur on the network.
- Integration a frontend UI

### Improvements (from challenges)
- Use coroutine instead of nromal threads for scheduling tasks and asynchronous calls
    - Challenge:
        - I read that it is better to take advantage of kotlin's coroutine libraries so I have tried to learn and implement coroutines as appropriate in the 
ERC721SubscriptionService class, but struggled to write the associated tests involving these coroutines.
    - Future fix:
        - Having learned how to use Mockk for testing and how it can handle coroutine testing much more easily, I would try to re-implement to use coroutine to handle asynchrnous calls and threading currently used.

### What I have learned
- How to use ScheduledExecutorService and ExecutorService from scratch (so apologies in advance if improvements on setting up and cleaning up is needed still)
- How to use Web3j library (I have only used Web3js library for a Typescript project in the past)
- How to use Mockk testing library
- How to use testcontainers to run a postgreSql instance

## Author
Jodi Chan
- Email: chanjodic@gmail.com
- LinkedIn: www.linkedin.com/in/jodi-chan

## License
MIT License

Copyright (c) 2024 Jodi Chan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
