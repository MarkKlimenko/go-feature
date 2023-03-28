CREATE TABLE namespaces
(
    id      VARCHAR(100) PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    status  VARCHAR(40)  NOT NULL,
    version INTEGER      NOT NULL
);
CREATE INDEX idx_namespaces_status ON namespaces (status);

CREATE TABLE filters
(
    id          VARCHAR(100) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    namespace   VARCHAR(100) NOT NULL REFERENCES namespaces (id),
    parameter   VARCHAR(100) NOT NULL,
    operator    VARCHAR(100) NOT NULL,
    status      VARCHAR(100) NOT NULL,
    description VARCHAR(4000),
    version     INTEGER      NOT NULL
);
CREATE INDEX idx_filters_namespace ON filters (namespace);
CREATE UNIQUE INDEX idx_unq_filters_name ON filters (name, namespace);
CREATE UNIQUE INDEX idx_unq_filters_params ON filters (namespace, parameter, operator);

CREATE TABLE features
(
    id          VARCHAR(100) PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    namespace   VARCHAR(100) NOT NULL REFERENCES namespaces (id),
    filters     VARCHAR      NOT NULL,
    status      VARCHAR(100) NOT NULL,
    description VARCHAR(4000),
    version     INTEGER      NOT NULL
);
CREATE INDEX idx_features_namespace ON filters (namespace);
CREATE UNIQUE INDEX idx_unq_features_name ON filters (name, namespace);

CREATE TABLE index_versions
(
    id            VARCHAR(100) PRIMARY KEY,
    namespace     VARCHAR(100) NOT NULL UNIQUE REFERENCES namespaces (id),
    index_version VARCHAR(100) NOT NULL,
    version       INTEGER      NOT NULL
);