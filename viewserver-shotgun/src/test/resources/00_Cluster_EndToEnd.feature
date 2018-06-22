Feature:Cluster scenarios

  Background:
    Given a running shotgun viewserver with url "inproc://master1" and version "^1.0.0" and bootstrap "true"
    Given a running shotgun viewserver with url "inproc://master2" and version "^2.0.0" and bootstrap "false"
    Given a running shotgun viewserver with url "inproc://master3" and version "^1.0.0" and bootstrap "false"
    Given keyColumn is "url"

  @MongoTest
  Scenario: All three clients should connect to compatible version
    Given a client named "client1" connected to "inproc://master1" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given a client named "client2" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "2.0.0"
    Given a client named "client3" connected to "inproc://master3" with authentication "compatibleVersion" and clientVersion "1.0.0"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 1             | true     |
      | inproc://master2 | ^2.0.0        | 1             | false    |
      | inproc://master3 | ^1.0.0        | 1             | false    |

  @MongoTest
  Scenario: Client should fail to connect to incompatible version but should be referred to compatible version
    Given a client named "client1" connected to "inproc://master1" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given a client named "client2" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "2.0.0"
    Given a client named "client3" connected to "inproc://master3" with authentication "compatibleVersion" and clientVersion "2.0.0"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 1             | true     |
      | inproc://master2 | ^2.0.0        | 2             | false    |
      | inproc://master3 | ^1.0.0        | 0             | false    |

  @MongoTest
  Scenario: When connecting to incompatible server Successive connections should be equally distributed amongst compatible view servers
    Given a client named "client1" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "1.0.0"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 1             | true     |
      | inproc://master2 | ^2.0.0        | 0             | false    |
      | inproc://master3 | ^1.0.0        | 0             | false    |
    Given a client named "client2" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 1             | true     |
      | inproc://master2 | ^2.0.0        | 0             | false    |
      | inproc://master3 | ^1.0.0        | 1             | false    |
    Given a client named "client3" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 2             | true     |
      | inproc://master2 | ^2.0.0        | 0             | false    |
      | inproc://master3 | ^1.0.0        | 1             | false    |
    Given a client named "client4" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 2             | true     |
      | inproc://master2 | ^2.0.0        | 0             | false    |
      | inproc://master3 | ^1.0.0        | 2             | false    |
    Given a client named "client5" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 3             | true     |
      | inproc://master2 | ^2.0.0        | 0             | false    |
      | inproc://master3 | ^1.0.0        | 2             | false    |
    Given a client named "client6" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "1.0.0"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 3             | true     |
      | inproc://master2 | ^2.0.0        | 0             | false    |
      | inproc://master3 | ^1.0.0        | 3             | false    |

  @MongoTest
  Scenario: When connecting to compatible server with authentication compatibleVersionEvenlyDistributed Successive connections should be equally distributed amongst compatible view servers
    Given a client named "client1" connected to "inproc://master1" with authentication "compatibleVersionEvenlyDistributed" and clientVersion "1.0.0"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections |
      | inproc://master1 | ^1.0.0        | 1             |
      | inproc://master2 | ^2.0.0        | 0             |
      | inproc://master3 | ^1.0.0        | 0             |
    Given a client named "client2" connected to "inproc://master1" with authentication "compatibleVersionEvenlyDistributed" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections |
      | inproc://master1 | ^1.0.0        | 1             |
      | inproc://master2 | ^2.0.0        | 0             |
      | inproc://master3 | ^1.0.0        | 1             |
    Given a client named "client3" connected to "inproc://master1" with authentication "compatibleVersionEvenlyDistributed" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections |
      | inproc://master1 | ^1.0.0        | 2             |
      | inproc://master2 | ^2.0.0        | 0             |
      | inproc://master3 | ^1.0.0        | 1             |
    Given a client named "client4" connected to "inproc://master1" with authentication "compatibleVersionEvenlyDistributed" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections |
      | inproc://master1 | ^1.0.0        | 2             |
      | inproc://master2 | ^2.0.0        | 0             |
      | inproc://master3 | ^1.0.0        | 2             |
    Given a client named "client5" connected to "inproc://master1" with authentication "compatibleVersionEvenlyDistributed" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections |
      | inproc://master1 | ^1.0.0        | 3             |
      | inproc://master2 | ^2.0.0        | 0             |
      | inproc://master3 | ^1.0.0        | 2             |
    Given a client named "client6" connected to "inproc://master3" with authentication "compatibleVersionEvenlyDistributed" and clientVersion "1.0.0"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 3             | true     |
      | inproc://master2 | ^2.0.0        | 0             | false    |
      | inproc://master3 | ^1.0.0        | 3             | false    |


  @MongoTest
  Scenario: Killing master should delegate master responsiblity to node in cluster with least connections
    Given a client named "client1" connected to "inproc://master1,inproc://master3" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster | isOffline |
      | inproc://master1 | ^1.0.0        | 0             | true     | false     |
      | inproc://master2 | ^2.0.0        | 0             | false    | false     |
      | inproc://master3 | ^1.0.0        | 1             | false    | false     |
    Given a client named "client2" connected to "inproc://master1,inproc://master3" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given a client named "client3" connected to "inproc://master1,inproc://master3" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given a client named "client4" connected to "inproc://master3,inproc://master1" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given a client named "client5" connected to "inproc://master3,inproc://master1" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given a client named "client6" connected to "inproc://master3,inproc://master1" with authentication "compatibleVersion" and clientVersion "1.0.0"
    And sleep for 3000 millis
    And Shotgun viewserver with url "inproc://master1" is killed
    And sleep for 15000 millis
    When "client6" subscribed to report "cluster"
    Then "client6" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster | isOffline |
      | inproc://master1 | ^1.0.0        | 0             | false    | true      |
      | inproc://master2 | ^2.0.0        | 0             | true     | false     |
      | inproc://master3 | ^1.0.0        | 6             | false    | false     |

  @MongoTest
  Scenario: Incompatible client should not connect
    Given a client named "client1" connected to "inproc://master1" with authentication "compatibleVersion" and clientVersion "1.0.0"
    Given a client named "client2" connected to "inproc://master2" with authentication "compatibleVersion" and clientVersion "2.0.0"
    Given a client named "client3" connect failed to "inproc://master3" with authentication "compatibleVersion" and clientVersion "3.0.0"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster" snapshot
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | ^1.0.0        | 1             | true     |
      | inproc://master2 | ^2.0.0        | 1             | false    |
      | inproc://master3 | ^1.0.0        | 0             | false    |
