USE db;
DROP TABLE IF EXISTS db.IOUs;
DROP TABLE IF EXISTS db.Lives;

CREATE TABLE IOUs (
      fname VARCHAR(20),
      lname VARCHAR(20),
      amount INT);
/* We replaced DOUBLE with INT, because the datatype conversion is different across databases, and this db case is not relevant to that aspect */
INSERT INTO IOUs (fname, lname, amount) VALUES ('Bob', 'Smith', 30);
INSERT INTO IOUs (fname, lname, amount) VALUES ('Sue', 'Jones', 20);
INSERT INTO IOUs (fname, lname, amount) VALUES ('Bob', 'Smith', 30);
CREATE TABLE Lives (
      fname VARCHAR(20),
      lname VARCHAR(20),
      city VARCHAR(20));
INSERT INTO Lives (fname, lname, city) VALUES ('Bob', 'Smith', 'London');
INSERT INTO Lives (fname, lname, city) VALUES ('Sue', 'Jones', 'Madrid');
INSERT INTO Lives (fname, lname, city) VALUES ('Bob', 'Smith', 'London');