# Token Transfer Indexer Application

## Description

### What it does
This application tracks and indexes ERC-721 Token Transfer events on the Ethereum network. It allow users to query all 
indexed ERC-721 token transfer events that occured while the application is running. It also enables users to check the connection status
and manually retry establishing the connection when the connnection fails.

### Main technologies used
#### General
- Kotlin (version 1.9.23)
- Spring Boot (versiion 3.2.5)
- Gradle (version 7.4)
- PostgrSQL

#### Blockchain related
- Infura, for reliable websocket endpoint to EVM chain (https://www.infura.io/).
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
   - Note this is required for running both the application and for Erc721UtilsTest.kt (as this tesst makes a real API call using this API key).
    
#### 2. Have a local PostgreSQL database instance running
Please ensure you have a local postgreSQL server running with a database created.

1. Provide the following value for the application.yaml file from your running instance (root path: src/main/resources/application.yaml):
  - Valid username.
  - Valid password.
  - Valid database.
  - Also replace the following bolded word with a valid database name in datasource.url: `jdbc:postgresql://localhost:5432/**postgres**`

### Usage
To run application:
1. Find TokenTransferIndexerApplication.kt (root path: src/main/kotlin/com/example/demo/TokenTransferIndexerApplication.kt)
2. Left click on file and select 'Run 'TokenTransferIndexer''

## API Reference

### Subscription controller
#### Retry establishing subscription connection
- **Endpoint**: `/api/subscription/erc721/retry`
- **Method**: `POST`
- **Description**: Endpoint to trigger manual retry when subscription connection fails to establish.
- **Response**:
  - `200 OK`: Returns a String with connection status message.
    `Retry successful.` or `Connection already established.`
Note: For future works, I would refactor this so that a different status code is returned for the different scenarios.

#### Retry establishing subscription connection
- **Endpoint**: `/api/subscription/status`
- **Method**: `GET`
- **Description**: Endpoint to get current status of subscription connection.
- **Response**:
  - `200 OK`: Returns a String with connection status message.
    `Subscription is active.` or `Subscription is not active.`
Note: For future works, I would refactor this so that a different status code is returned for the different scenarios.

### Token indexer controller
#### Retry establishing subscription connection
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
            "logIndex": "Nullable String",
            "senderAddress": "Nullable String",
            "receiverAddress": "Nullable String",
            "tokenId": "Nullable String",
            "contractAddress": "Nullable String"
         }
      ```
   - Fields:
      - contractAddress (string, optional): The contract address of the ERC-721 token.
      - tokenId (string, optional): The ID of the ERC-721 token.
      - fromAddress (string, optional): The address from which the token was transferred.
      - toAddress (string, optional): The address to which the token was transferred.
      - txHash (string, optional): The hash of the transaction.
      - blockNumber (integer, optional): The block number where the transaction was included.
- **Query Parameters**:
   - Page (integer, optional): Page number of the results to retrieve. Default is 0.
   - Size (integer, optional): Number of entries per page. Default is 20.
   - Sort (string, optional): Sorting criteria in the format property,asc|desc. Multiple sort criteria are supported.
- **Response**:
  - `200 OK`: Returns a Page object containing a lsit of ERC-721 tokens.
     - Schema:
         - ```
           {
              "content": [
                {
                  "txHash": "String",
                  "blockNumber": "Big Integer",
                  "logIndex": "String",
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
Run it again and it almost always pass. Apologies for the inconvenience for the time being.

### Usage
To run all tests:
1. Left click on src/test directory and select "Run tests in demo tests" using your IDE.
To run individual tests instead:
1. Feft click on a test file and select "Run" using your IDE. 

## Future work
### Add more features
- Implement automatic retrieval using previous blockNumbers in the database to find missing ERC-721 transfer token events from when the application last ran.
- Expand monitoring and indexing of other token transfers that occur on the network.
- Integration a frontend UI.
- Implement support for indexing ERC721 metadata especially those on IPFS, which would be relevant and valuable to users querying ERC-721 token events.

### Improvements (from challenges)
- Use coroutine instead of normal threads for scheduling tasks and asynchronous calls.
    - Challenge:
        - I read that it is better to take advantage of kotlin's coroutine libraries so I have tried to learn and implement coroutines as appropriate in the 
ERC721SubscriptionService class, but struggled to write the associated tests involving these coroutines.
    - Future fix:
        - Having learned how to use Mockk for testing and how it can handle coroutine testing much more easily, I would try to re-implement to use coroutine to handle asynchrnous calls and threading currently used.

### What I have learned
- How to use ScheduledExecutorService and ExecutorService from scratch (apologies if improvements on setting up and cleaning up is still needed).
- How to use Web3j library (I have only used Web3js library for a small Typescript project in the past).
- How to use the Mockk testing library.
- How to use testcontainers to run a postgreSql instance for integration tests.

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
