CREATE TABLE namespaces
(
    id      VARCHAR(100) PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    status  VARCHAR(40)  NOT NULL,
    version INTEGER      NOT NULL
);
CREATE INDEX namespaces_status_idx ON namespaces (status);

CREATE TABLE index_versions
(
    id            VARCHAR(100) PRIMARY KEY,
    namespace     VARCHAR(100) NOT NULL UNIQUE REFERENCES namespaces (id),
    index_version VARCHAR(100) NOT NULL,
    version       INTEGER      NOT NULL
);