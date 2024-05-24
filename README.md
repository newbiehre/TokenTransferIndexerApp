# Token Transfer Indexer Application

## Description

### What it does
This application tracks and indexes ERC-721 Token Transfer events on the Ethereum network. It allow users to query all 
indexed ERC-721 token transfer events that occured in real-time while the application is running. It also enable users to check the connection status
and manually retry reestablishing the connection when the connnection fails. 

There is also a log file that logs the activities taking place while 
the application runs. The logs are stored in logs/tokenIndexerApp.log file.

Please read the documentation within the source code of all .kt files in the main and test directory for more details.

### Main technologies used
#### General
- Kotlin (version 1.9.23)
- Spring Boot (versiion 3.2.5)
- Gradle (version 7.4)
- PostgreSQL

#### Blockchain related
- Infura, a popular and reliable websocket endpoint to the Ethereum main network (https://www.infura.io/).
- Web3j, a library compatible with JVM languages for interacting with ethereum client (https://docs.web3j.io/4.11.0/).

## Table of Contents
- [Getting started](#getting-started)
- [API reference](#api-reference)
- [Tests](#tests)
- [Future work](#future-work)
- [Author](#author)
- [License](#license)

## Getting started

### Prerequisites
Please ensure you are using the following:
- JDK version 21.0.2
```
java --version
openjdk 21.0.2 2024-01-16
OpenJDK Runtime Environment (build 21.0.2+13-58)
OpenJDK 64-Bit Server VM (build 21.0.2+13-58, mixed mode, sharing)
```
Then click on the build gradle button on your IDE (or run terminal command `./gradlew build` to download all dependencies and compile the code).

#### 1. Open a Infura Account and obtain an API key
1. Login or create a Infura account (https://www.infura.io/).
2. You should be taken to the API Keys page after authentication.
3. Create a new API key or select an existing one.
4. Ensure the following is checked and saved:
   - Endpoint networks ethereum: mainnet (doesn't matter if you checked the other options as we won't be using it).
   - ![image](https://github.com/newbiehre/TokenTransferIndexerApp/assets/58487237/ecc3ef27-466a-49da-b68e-0d530b6292cf)
5. Copy the API key and paste it in infura.apiKey in application.yaml file (root path: src/main/resources/application.yaml).
   - Note this is required for running both the application and for Erc721UtilsTest.kt (as this tesst makes a real API call using this API key).
    
#### 2. Have a local PostgreSQL database instance running
Please ensure you have a local postgreSQL server running with a database created.
(https://www.youtube.com/watch?v=PShGF_udSpk&ab_channel=ProgrammingKnowledge -> Tutorial on how to install, configure and use PostgreSQL on Mac computers).

1. Provide the following value for the application.yaml file from your running instance (root path: src/main/resources/application.yaml):
  - Valid username.
  - Valid password.
  - Valid database.
  - Also replace the following bolded word with a valid database name in datasource.url: `jdbc:postgresql://localhost:5432/**postgres**`.

### Usage
To run the application:
1. Find TokenTransferIndexerApplication.kt (root path: src/main/kotlin/com/example/demo/TokenTransferIndexerApplication.kt).
2. Left click on file and select 'Run 'TokenTransferIndexer''.

## API Reference

### Subscription REST controller
#### Retry establishing subscription connection
- **Endpoint**: `/api/subscription/erc721/retry`
- **Method**: `POST`
- **Description**: Endpoint to trigger manual retry when subscription connection fails to establish.
- **Response**:
  - `200 OK`: Returns a String with connection status message.
    `Retry successful.` or `Connection already established.`
Note: For future works, I would refactor this so that a different status code is returned for the different scenarios.

#### Retrieve subscription connection status
- **Endpoint**: `/api/subscription/status`
- **Method**: `GET`
- **Description**: Endpoint to get current status of subscription connection.
- **Response**:
  - `200 OK`: Returns a String with connection status message `Subscription is active.` or `Subscription is not active.`
Note: For future works, I would refactor this so that a different status code is returned for the different scenarios.

### Token indexer REST controller
#### Query the database for ERC-721 Token Transfer events that occured over the Ethereum network.
- **Endpoint**: `/api/subscription/erc721/retry`
- **Method**: `POST`
- **Description**: Endpoint for querying ERC-7821 token transfer events that occured while the application was running based on provided filter criteria.
- **Request Body**:
  - Content-Type: application/json
  - Schema:
       ```
        {
            "txHash": "Nullable String",
            "blockNumber": "Nullable Big Integer",
            "logIndex": "Nullable Big Integer",
            "senderAddress": "Nullable String",
            "receiverAddress": "Nullable String",
            "tokenId": "Nullable String",
            "contractAddress": "Nullable String"
         }
      ```
   - Fields:
      - txHash (String, optional): Unique identifier for the transaction, generated based on the transaction details.
      - blockNumber (Big Integer, optional): The block number in which the transaction was included.
      - logIndex (Big Integer, optional): Index position of the log entry within the block, useful for identifying specific events.
      - senderAddress (String, optional): Ethereum address from which the token was sent.
      - receiverAddress (String, optional): Ethereum address to which the token was sent.
      - tokenId (String, optional): Unique identifier for the token within the ERC721 contract.
      - contractAddress (String, optional): Address of the ERC721 contract, unique for each deployed contract.
- **Query Parameters**:
   - Page (Integer, optional): Page number of the results to retrieve. Default is 0.
   - Size (Integer, optional): Number of entries per page. Default is 20.
   - Sort (String, optional): Sorting criteria in the format property, asc|desc. Multiple sort criteria are supported.
- **Response**:
  - `200 OK`: Returns a Page object containing a list of ERC-721 tokens.
     - Schema:
         - ```
           {
              "content": [
                {
                  "txHash": "String",
                  "blockNumber": "Big Integer",
                  "logIndex": "Big Integer",
                  "senderAddress": "String",
                  "receiverAddress": "String",
                  "tokenId": "String",
                  "contractAddress": "String"
                }
              ],
              "pageable": {
                "sort": {
                  "sorted": false,
                  "unsorted": false,
                  "empty": true
                },
                "pageNumber": 0,
                "pageSize": 20,
                "offset": 0,
                "paged": true,
                "unpaged": false
              },
              "totalPages": 1,
              "totalElements": 1,
              "last": false,
              "first": true,
              "sort": {
                "sorted": true,
                "unsorted": false,
                "empty": false
              },
              "numberOfElements": 1,
              "size": 20,
              "number": 0,
              "empty": false
            }
           }
           ```
   - Example reponse:
        - ```
            {
             "content": [
                 {
                  "txHash": "0xe2d990a94bcb79f3111333b52ef8809f158ac226595928f36f8cadd144970129",
                  "blockNumber": 19924777,
                  "logIndex": 272,
                  "senderAddress": "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef",
                  "receiverAddress": "0x0000000000000000000000000000000000000000000000000000000000000000",
                  "tokenId": "0x000000000000000000000000deface9dc31657539447b7a85e673bb3e0eff248",
                  "contractAddress": "0x762c2dd38515e8c3c9b216896e27894ef37b68de"
                }
          ],
          "pageable": {
              "pageNumber": 0,
              "pageSize": 20,
              "sort": {
                  "empty": true,
                  "unsorted": true,
                  "sorted": false
              },
              "offset": 0,
              "paged": true,
              "unpaged": false
          },
          "last": true,
          "totalElements": 1,
          "totalPages": 1,
          "first": true,
          "size": 20,
          "number": 0,
          "sort": {
              "empty": true,
              "unsorted": true,
              "sorted": false
          },
          "numberOfElements": 1,
          "empty": false
            }
          ```

## Tests

### Technologies used
- Mockito-kotlin (version 3.2.0)
- Mockk (versiion 3.2.5)
- Testcontainers (version 1.19.8)
- Testcontainers:postgresql (version 1.19.8)

### Set up
For running integration tests involving database testing (Tests: Erc721TokenDaoTest.kt, Erc721TokenRepositoryTest.kt):
1. Open application-test.yaml file (root path: src/test/resources/application-test.yaml)
2. Provide a valid username and password to your PostgreSQL database (can be the same as your application.yaml values).
3. Install Docker Desktop and ensure a Docker instance is running (installation guide: https://docs.docker.com/desktop/).

For Erc721UtilsTest.kt:
1. It utilizes application.yaml file (root path: src/main/resources/application.yaml) to retreive infura.apiKey.
Ensure a valid api key value is provided before running the test.

### Warning
The following test doesn't consistently pass on the first go: 
- Erc721SubscriptionServiceTest's Batch Processing test 
"Should not need to verify contract address (call checkIfErc721())) if found in database" (root path: src/test/kotlin/com/example/demo/services/subscription/Erc721SubscriptionServiceTest.kt).
- Run it again and it will almost always pass. Apologies for the inconvenience. For future works, I would fix this to ensure it will always pass the test consistently

### Usage
To run all tests:
1. Left click on src/test directory and select "Run tests in demo tests" using your IDE.
To run the tests individually:
1. Feft click on a test file and select "Run" using your IDE. 

## Future work

### Add more features
- Implement automatic retrieval using previous blockNumbers in the database to find missing ERC-721 transfer token events from when the application was last running.
- Integrate a frontend UI.
- Implement support for indexing ERC721 metadata especially those on IPFS, which would be relevant and valuable to users querying ERC-721 token events.
- Expand monitoring and indexing of other token transfers that occur on the network.
   - The application is structured in a way that would easily accomodate additional token types of tokens and events. All files named Erc721* is ERC-721 specific, while the rest
of the file is meant to be shared. Of course, this may change as needed.

### Improvements (from challenges)
- Use coroutine instead of normal threads for scheduling tasks and asynchronous calls.
    - Challenge:
        - I read that it is better to take advantage of kotlin's coroutine libraries so I have tried to learn and implement coroutines as appropriate in the 
ERC721SubscriptionService class, but struggled to write the associated tests involving these coroutines.
    - Future fix:
        - Having learned how to use Mockk for testing and how it can handle coroutine testing much more easily, I would try to re-implement to use coroutine to handle asynchrnous calls and threading currently used.

### What I have learned
- How to use ScheduledExecutorService and ExecutorService from scratch (apologies if improvements on setting up and cleaning up is still needed).
   - This is used to make the subscription service more performant for handling such frequently emitted events, for the asynchronous call to the blockchain, and frequent
   - need to access and store data into the database.
   - As mentioned earlier, I wanted to use coroutine instead. Unfortunately I did not have enough time to how to correctly test coroutine tests.
- How to use Web3j library (I have only used Web3js library for a small Typescript project in the past).
- How to use the Mockk testing library.
- How to use testcontainers to run a postgreSql instance for integration tests.

## Author
Jodi Chan
- Email: chanjodic@gmail.com
- LinkedIn: www.linkedin.com/in/jodi-chan

If you have any questions or clarifications, please feel free to reach out!

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
