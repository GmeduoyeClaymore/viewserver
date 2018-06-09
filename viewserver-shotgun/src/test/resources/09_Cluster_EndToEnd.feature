Feature:Cluster scenarios

  Background:
    Given a running shotgun viewserver with url "inproc://master1" and version "1" and bootstrap "true"
    Given a running shotgun viewserver with url "inproc://master2" and version "2" and bootstrap "false"
    Given a running shotgun viewserver with url "inproc://master3" and version "1" and bootstrap "false"
    Given keyColumn is "url"

  @MongoTest
  Scenario: All three clients should connect to compatable version
    Given a client named "client1" connected to "inproc://master1" with authentication "compatableVersion" and token "1"
    Given a client named "client2" connected to "inproc://master2" with authentication "compatableVersion" and token "2"
    Given a client named "client3" connected to "inproc://master3" with authentication "compatableVersion" and token "1"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster"
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | 1             | 1             | true     |
      | inproc://master2 | 2             | 1             | false    |
      | inproc://master3 | 1             | 1             | false    |

  @MongoTest
  Scenario: Client should fail to connect to incompatable version but should be referred to compatable verison
    Given a client named "client1" connected to "inproc://master1" with authentication "compatableVersion" and token "1"
    Given a client named "client2" connected to "inproc://master2" with authentication "compatableVersion" and token "2"
    Given a client named "client3" connected to "inproc://master3" with authentication "compatableVersion" and token "2"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster"
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | 1             | 1             | true     |
      | inproc://master2 | 2             | 2             | false    |
      | inproc://master3 | 1             | 0             | false    |

  @MongoTest
  Scenario: When connecting to incompatable server Successive connections should be equally distributed amongst compatable view servers
    Given a client named "client1" connected to "inproc://master2" with authentication "compatableVersion" and token "1"
    Then sleep for 1000 millis
    Given a client named "client2" connected to "inproc://master2" with authentication "compatableVersion" and token "1"
    Then sleep for 1000 millis
    Given a client named "client3" connected to "inproc://master2" with authentication "compatableVersion" and token "1"
    Then sleep for 1000 millis
    Given a client named "client4" connected to "inproc://master2" with authentication "compatableVersion" and token "1"
    Then sleep for 1000 millis
    Given a client named "client5" connected to "inproc://master2" with authentication "compatableVersion" and token "1"
    Then sleep for 1000 millis
    Given a client named "client6" connected to "inproc://master2" with authentication "compatableVersion" and token "1"
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster"
     | url              | clientVersion | noConnections | isMaster |
     | inproc://master1 | 1             | 3             | true     |
     | inproc://master2 | 2             | 0             | false    |
     | inproc://master3 | 1             | 3             | false    |

  @MongoTest
  Scenario: When connecting to compatable server with authentication compatableVersionEvenlyDistributed Successive connections should be equally distributed amongst compatable view servers
    Given a client named "client1" connected to "inproc://master1" with authentication "compatableVersionEvenlyDistributed" and token "1"
    Then sleep for 1000 millis
    Given a client named "client2" connected to "inproc://master1" with authentication "compatableVersionEvenlyDistributed" and token "1"
    Then sleep for 1000 millis
    Given a client named "client3" connected to "inproc://master1" with authentication "compatableVersionEvenlyDistributed" and token "1"
    Then sleep for 1000 millis
    Given a client named "client4" connected to "inproc://master1" with authentication "compatableVersionEvenlyDistributed" and token "1"
    Then sleep for 1000 millis
    Given a client named "client5" connected to "inproc://master1" with authentication "compatableVersionEvenlyDistributed" and token "1"
    Then sleep for 1000 millis
    Given a client named "client6" connected to "inproc://master3" with authentication "compatableVersionEvenlyDistributed" and token "1"
    Then sleep for 1000 millis
    When "client1" subscribed to report "cluster"
    Then "client1" the following data is received eventually on report "cluster"
      | url              | clientVersion | noConnections | isMaster |
      | inproc://master1 | 1             | 3             | true     |
      | inproc://master2 | 2             | 0             | false    |
      | inproc://master3 | 1             | 3             | false    |

  @MongoTest
  Scenario: Killing master should delegate master responsiblity to node in cluster with least connections
    Given a client named "client1" connected to "inproc://master1,inproc://master3" with authentication "compatableVersion" and token "1"
    Given a client named "client2" connected to "inproc://master1,inproc://master3" with authentication "compatableVersion" and token "1"
    Given a client named "client3" connected to "inproc://master1,inproc://master3" with authentication "compatableVersion" and token "1"
    Given a client named "client4" connected to "inproc://master3,inproc://master1" with authentication "compatableVersion" and token "1"
    Given a client named "client5" connected to "inproc://master3,inproc://master1" with authentication "compatableVersion" and token "1"
    Given a client named "client6" connected to "inproc://master3,inproc://master1" with authentication "compatableVersion" and token "1"
    And Shotgun viewserver with url "inproc://master1" is killed
    And sleep for 10000 millis
    When "client6" subscribed to report "cluster"
    Then "client6" the following data is received eventually on report "cluster"
      | url              | clientVersion | noConnections | isMaster | isOffline |
      | inproc://master1 | 1             | 0             | false    | true      |
      | inproc://master2 | 2             | 0             | true     | false     |
      | inproc://master3 | 1             | 6             | false    | false     |
