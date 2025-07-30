# Flight App
A command-line airline management system built in Java with PostgreSQL. Users can create accounts, search for flights, book itineraries, and manage their reservations. The system implements core data management principles including relational schema design, SQL queries, ACID transactions, password hashing, and JDBC-based application logic.

(Note: This project was originally designed to run on a university-hosted database server with restricted access, so please refer to the code and documentation instead!)

## 🤸🏽‍♀️ Skills
- **Programming:** Java, SQL, JDBC (Java Database Connectivity) API, Maven, JUnit
- **Database:** PostgreSQL, relational schema design, ER modeling
- **Transactions:** SQL transaction management (ACID)
- **Security:** salted password hashing, SQL injection prevention via PreparedStatements
- **Software engineering:** CLI-based application structure, modular design, test-driven development

## 😶‍🌫️ What I Learned
- How to design and implement normalized relational schemas
- Experience building a user-driven interface that interacts with a live database backend
- Practice writing complex SQL queries
- How to coordinate SQL transactions to prevent race conditions
- How to handle user authentication securely using salted and hashed passwords

## 🖌️ Design Notes
- Schema normalization separates users, reservations, and flight metadata
- Direct and indirect itineraries are unified under a single booking interface
- All user interactions are handled outside SQL transactions

A piece of the ER Diagram:
<img width="967" height="130" alt="image" src="https://github.com/user-attachments/assets/002bab01-5390-416a-b257-15c5a9daaed0" />

## ✈️ Features
Users
- `create <username> <password> <balance>`
- `login <username> <password>`

Flight search
- `search <origin> <dest> <0/1 flag indicating whether to return only direct flights> <day in October (int)> <max num itins to return>`
- Sorts results by total flight time, with direct itineraries preferred
- Automatically filters out canceled flights 

Reservations
- `book <itin num>` based on most recent search; enforces capacity constraints
- `pay <reservation num>`
- `reservations` lists the logged-in user's reservations, displayed in format similar to `search`

Security & transactions
- Prevents SQL injection using PreparedStatements
- Salts and hashes passwords, stored securely as bytea
- Ensures ACID properties and prevents race conditions using SQL transactions

## 🌳 Directory Overview
```
.
├── src/
│   └── main/java/flightapp/
│       └── QueryAbstract.java
│       └── Query.java          # (Java + SQL) Query logic, transactions
│       └── PasswordUtils.java  # Salts, hashes, verifies passwords
│       └── FlightService.java  # Entry point for the CLI app
│       └── DBConnUtils.java    # Handles DB connections
│   └── test/java/flightapp/    # Unit tests (JUnit)
├── cases/                      # (.txt) My transactional test cases, following a UW Allen School-specific test harness
├── createTables.sql            # Schema definition
└── pom.xml                     # Maven configuration
```

## Credits
This project was originally developed as part of UW's CSE 344: Data Management taught by Prof. Hannah Tang. The core design belongs to the course staff. This implementation and documentation reflect my own independent work and enhancements.
