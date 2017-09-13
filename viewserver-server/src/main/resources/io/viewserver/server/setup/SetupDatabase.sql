create table if not exists DataSources (
    name nvarchar(255) primary key,
    status nvarchar(50),
    json nclob
);

create table if not exists Reports (
    id nvarchar(255) primary key,
    name nvarchar(255),
    dataSource nvarchar(255),
    json nclob
);
