package flightapp;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.sql.Types;
import java.sql.*;

/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  //
  // Canned queries
  //
  private static final String FLIGHT_CAPACITY_SQL = "SELECT num_seats " +
                                                    "FROM N_Numbers AS N, Aircraft_Types AS A " +
                                                    "WHERE N.mfr_mdl_code = A.atid AND N.n_number = ?";
  private PreparedStatement flightCapacityStmt;

  private static final String GET_USER_SQL = "SELECT * FROM User_cartierc WHERE username = ?";
  private PreparedStatement getUserStmt;

  private static final String DEL_USER_SQL = "DELETE FROM User_cartierc";
  private PreparedStatement delUsersStmt;

  private static final String DEL_RES_SQL = "DELETE FROM Reservation_cartierc";
  private PreparedStatement delResStmt;

  private static final String INSERT_USER_SQL = "INSERT INTO User_cartierc VALUES (?, ?, ?)";
  private PreparedStatement insertUserStmt;

  private static final String DIRECT_SQL = "SELECT fid, day_of_month, cid, tail_num, op_carrier_flight_num, " +
                                           "origin_city, dest_city, duration_mins, price " +
                                           "FROM Flights " +
                                           "WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? AND cancelled = 0";
  private PreparedStatement directStmt;

  private static final String INDIRECT_SQL = "SELECT F1.fid AS f1Id, F1.day_of_month AS f1Day, F1.cid as f1Carrier, " +
                                             "F1.tail_num AS f1TailNum, F1.op_carrier_flight_num AS f1FNum, F1.origin_city AS f1Origin, " +
                                             "F1.dest_city AS f1Dest, F1.duration_mins AS f1Tm, F1.price AS f1Pri, " +
                                             "F2.fid AS f2Id, F2.day_of_month AS f2Day, F2.cid as f2Carrier, " +
                                             "F2.tail_num AS f2TailNum, F2.op_carrier_flight_num AS f2FNum, F2.origin_city AS f2Origin, " +
                                             "F2.dest_city AS f2Dest, F2.duration_mins AS f2Tm, F2.price AS f2Pri " + 
                                             "FROM Flights F1, Flights F2 WHERE F1.dest_city = F2.origin_city AND F1.fid != F2.fid " +
                                             "AND F1.origin_city = ? AND F2.dest_city = ? AND F1.day_of_month = ? AND F2.day_of_month = ? " + 
                                             "AND F1.cancelled = 0 AND F2.cancelled = 0";
  private PreparedStatement indirectStmt;

  private static final String NUM_BOOKED_ON_SQL = "SELECT COUNT(*) AS count " +
                                                  "FROM Reservation_cartierc AS R, Flights AS F " +
                                                  "WHERE R.f1id = F.fid AND F.day_of_month = ? AND username = ?";
  private PreparedStatement numBookedOnStmt;

  private static final String NUM_RESERVED_ON_F1_SQL = "SELECT COUNT(*) AS count FROM Reservation_cartierc WHERE f1id = ?";
  private PreparedStatement numReservedOnF1Stmt;

  private static final String NUM_RESERVED_ON_F2_SQL = "SELECT COUNT(*) AS count FROM Reservation_cartierc WHERE f2id = ?";
  private PreparedStatement numReservedOnF2Stmt;

  private static final String INSERT_RES_SQL = "INSERT INTO Reservation_cartierc VALUES (?, ?, ?, ?, ?)";
  private PreparedStatement insertResStmt;

  private static final String NUM_RES_SQL = "SELECT COUNT(*) AS count FROM Reservation_cartierc";
  private PreparedStatement numResStmt;

  private static final String GET_RES_SQL = "SELECT * FROM Reservation_cartierc WHERE rid = ?";
  private PreparedStatement getResStmt;

  private static final String FLIGHT_PRICE_SQL = "SELECT price FROM Flights WHERE fid = ?";
  private PreparedStatement flightPriceStmt;

  private static final String PAY_RES_SQL = "UPDATE Reservation_cartierc SET isPaidFor = 1 WHERE rid = ?";
  private PreparedStatement payResStmt;

  private static final String LOWER_BAL_SQL = "UPDATE User_cartierc SET balance = balance - ? WHERE username = ?";
  private PreparedStatement lowerBalStmt;

  private static final String GET_RES_FOR_SQL = "SELECT * FROM Reservation_cartierc WHERE username = ? ORDER BY rid";
  private PreparedStatement getResForStmt;

  private static final String GET_FLIGHT_SQL = "SELECT fid, day_of_month, cid, tail_num," +
                                               "op_carrier_flight_num, origin_city, dest_city, duration_mins, price " +
                                               "FROM Flights WHERE fid = ?";
  private PreparedStatement getFlightStmt;

  //
  // Instance variables
  //
  private final int MAX_TRIES = 9;
  private String currUser = null;
  private List<Itinerary> itineraries = new ArrayList<>();

  protected Query() throws SQLException, IOException {
    prepareStatements();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      // TODO: YOUR CODE HERE
      delResStmt.executeUpdate();
      delUsersStmt.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    flightCapacityStmt = conn.prepareStatement(FLIGHT_CAPACITY_SQL);

    // TODO: YOUR CODE HERE
    getUserStmt = conn.prepareStatement(GET_USER_SQL);
    delUsersStmt = conn.prepareStatement(DEL_USER_SQL);
    delResStmt = conn.prepareStatement(DEL_RES_SQL);
    insertUserStmt = conn.prepareStatement(INSERT_USER_SQL);
    directStmt = conn.prepareStatement(DIRECT_SQL);
    indirectStmt = conn.prepareStatement(INDIRECT_SQL);
    numBookedOnStmt = conn.prepareStatement(NUM_BOOKED_ON_SQL);
    numReservedOnF1Stmt = conn.prepareStatement(NUM_RESERVED_ON_F1_SQL);
    numReservedOnF2Stmt = conn.prepareStatement(NUM_RESERVED_ON_F2_SQL);
    insertResStmt = conn.prepareStatement(INSERT_RES_SQL);
    numResStmt = conn.prepareStatement(NUM_RES_SQL);
    getResStmt = conn.prepareStatement(GET_RES_SQL);
    flightPriceStmt = conn.prepareStatement(FLIGHT_PRICE_SQL);
    payResStmt = conn.prepareStatement(PAY_RES_SQL);
    lowerBalStmt = conn.prepareStatement(LOWER_BAL_SQL);
    getResForStmt = conn.prepareStatement(GET_RES_FOR_SQL);
    getFlightStmt = conn.prepareStatement(GET_FLIGHT_SQL);
  }

  // Rolls back conn; catches SQLException if one occurs
  private void rollback(Connection conn) {
    try {
      conn.rollback();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Sets conn to auto commit or not based on boolean param; catches SQLException if one occurs
  private void setAutoCommit(Connection conn, boolean autoCommit) {
    try {
      conn.setAutoCommit(autoCommit);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_login(String username, String password) {
    // TODO: YOUR CODE HERE
    if (currUser != null) return "User already logged in\n";

    final String failRet = "Login failed\n";
    try {
      getUserStmt.clearParameters();
      getUserStmt.setString(1, username);

      setAutoCommit(conn, false);
      ResultSet userRs = getUserStmt.executeQuery();
      if(!userRs.next()) { // !userExists
        userRs.close();
        rollback(conn);
        setAutoCommit(conn, true);
        return failRet;
      }

      byte[] saltAndHash = userRs.getBytes("password");
      userRs.close();
       // wrong pw
      if (!PasswordUtils.plaintextMatchesSaltedHash(password, saltAndHash)) {
        rollback(conn);
        setAutoCommit(conn, true);
        return failRet;
      }
      currUser = username;
      itineraries.clear();
      conn.commit();
      setAutoCommit(conn, true);
      return "Logged in as " + username + "\n";

    } catch (SQLException e) {
      rollback(conn);
      setAutoCommit(conn, true);
      e.printStackTrace();
      return failRet;
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    // TODO: YOUR CODE HERE
    final String failRet = "Failed to create user\n";
    if (initAmount < 0) return failRet;
    
    for (int tries = 0; tries < MAX_TRIES; tries++) {
      try {
        insertUserStmt.clearParameters();
        insertUserStmt.setString(1, username);
        byte[] saltAndHash = PasswordUtils.saltAndHashPassword(password);
        insertUserStmt.setBytes(2, saltAndHash);
        insertUserStmt.setInt(3, initAmount);

        setAutoCommit(conn, false);
        if (userExists(username)) {
          rollback(conn);
          setAutoCommit(conn, true);
          return failRet;
        }
        insertUserStmt.executeUpdate();
        conn.commit();
        setAutoCommit(conn, true);
        return "Created user " + username + "\n";

      } catch (SQLException e) {
        rollback(conn);
        setAutoCommit(conn, true);
        if (!isDeadlock(e)) {  // permanent
          e.printStackTrace();
          return failRet;
        }  // else if transient deadlock, try again
      }
    }
    return failRet;
  }

  /**
   * Returns true if user with username already exists in db, false if not.
   */
  private boolean userExists(String username) throws SQLException {
    getUserStmt.clearParameters();
    getUserStmt.setString(1, username);
    ResultSet rs = getUserStmt.executeQuery();
    boolean rowExists = rs.next();
    rs.close();
    return rowExists;
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_search(String originCity, String destinationCity, 
                                   boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries) {
    // TODO: YOUR CODE HERE
    if (numberOfItineraries <= 0) return "Failed to search\n";

    itineraries.clear();
    fillItineraryList(originCity, destinationCity, directFlight, dayOfMonth, numberOfItineraries);
    if (itineraries.size() == 0) return "No flights match your selection\n";

    return makeSearchString(itineraries);
  }

  // Returns list of itineraries, only direct if directFlight is true, size numberOfItineraries,
  // going from originCity to destinationCity,
  // in ascending order by total time
  // indices correspond with itinerary IDs
  private List<Itinerary> fillItineraryList(String originCity, String destinationCity, 
                                          boolean directFlight, int dayOfMonth,
                                          int numberOfItineraries) {
    // check non-null for Coll.sort()
    if (itineraries == null) throw new IllegalStateException("itineraries is null, shouldn't happen");
    try {
      // direct itineraries
      directStmt.clearParameters();
      directStmt.setString(1, originCity);
      directStmt.setString(2, destinationCity);
      directStmt.setInt(3, dayOfMonth);
      ResultSet directRs = directStmt.executeQuery();

      while (directRs.next()) {
        int capacity = getCapacity(directRs.getString("tail_num"));
        itineraries.add(new Itinerary(directRs.getInt("fid"), directRs.getInt("day_of_month"), directRs.getString("cid"),
                                      directRs.getInt("op_carrier_flight_num") + "", directRs.getString("origin_city"),
                                      directRs.getString("dest_city"), directRs.getInt("duration_mins"), capacity, directRs.getInt("price")));
      }
      directRs.close();
      // filter down directs if needed
      int numDirects = itineraries.size();
      if (numDirects > numberOfItineraries) {
        Collections.sort(itineraries);
        itineraries = itineraries.subList(0, numberOfItineraries);

      // indirect itineraries
      } else if (!directFlight && numDirects < numberOfItineraries) {
        indirectStmt.clearParameters();
        indirectStmt.setString(1, originCity);
        indirectStmt.setString(2, destinationCity);
        indirectStmt.setInt(3, dayOfMonth);
        indirectStmt.setInt(4, dayOfMonth);

        ResultSet indirectRs = indirectStmt.executeQuery();
        List<Itinerary> indirects = new ArrayList<>();
        while (indirectRs.next()) {
          int f1Capacity = getCapacity(indirectRs.getString("f1TailNum"));
          int f2Capacity = getCapacity(indirectRs.getString("f2TailNum"));
          indirects.add(new Itinerary(indirectRs.getInt("f1Id"), indirectRs.getInt("f1Day"), indirectRs.getString("f1Carrier"),
                                      indirectRs.getInt("f1FNum") + "", indirectRs.getString("f1Origin"), indirectRs.getString("f1Dest"),
                                      indirectRs.getInt("f1Tm"), f1Capacity, indirectRs.getInt("f1Pri"),
                                      indirectRs.getInt("f2Id"), indirectRs.getInt("f2Day"), indirectRs.getString("f2Carrier"),
                                      indirectRs.getInt("f2FNum") + "", indirectRs.getString("f2Origin"), indirectRs.getString("f2Dest"),
                                      indirectRs.getInt("f2Tm"), f2Capacity, indirectRs.getInt("f2Pri")));
        }
        indirectRs.close();

        // filter down indirects if needed
        Collections.sort(indirects);
        int numRemainingResults = numberOfItineraries - numDirects;
        if (indirects.size() > numRemainingResults) {
          indirects = indirects.subList(0, numRemainingResults);
        }
        // add indirects to results
        itineraries.addAll(indirects);
      }
      // sort all results
      Collections.sort(itineraries);
      return itineraries;

    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Given a flight's tail num, returns flight's capacity
  // If tail num not found, returns -1
  private int getCapacity(String tailNum) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setString(1, tailNum);
    ResultSet rs = flightCapacityStmt.executeQuery();
    int capacity = rs.next() ? rs.getInt("num_seats") : -1;
    rs.close();
    return capacity;
  }

  // Returns String of formatted itineraries from given list
  private String makeSearchString(List<Itinerary> itineraries) {
    StringBuffer sb = new StringBuffer();
    int i = 0;
    for (Itinerary itin : itineraries) {
      sb.append("Itinerary " + i + ": ");
      if (itin.f2 == null) { // direct
        sb.append("1 flight(s), " + itin.totalTime + " minutes\n" + itin.f1.toString() + "\n");
      } else { // indirect
        sb.append("2 flight(s), " + itin.totalTime + " minutes\n" + itin.f1.toString() + "\n" + itin.f2.toString() + "\n");
      }
      i++;
    }
    return sb.toString();
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_book(int itineraryId) {
    if (currUser == null) return "Cannot book reservations, not logged in\n";
    if (itineraryId < 0 || itineraryId >= itineraries.size()) return "No such itinerary " + itineraryId + "\n";

    final String failRet = "Booking failed\n";
    for (int tries = 0; tries < MAX_TRIES; tries++) {
      try {
        // more error checking (that does sql)
        Itinerary itinerary = itineraries.get(itineraryId);

        setAutoCommit(conn, false);
        if (alreadyBookedOn(itinerary.f1.dayOfMonth)) {
          rollback(conn);
          setAutoCommit(conn, true);
          return "You cannot book two flights in the same day\n";
        }
        if (!itinerary.hasSpace()) {
          rollback(conn);
          setAutoCommit(conn, true);
          return failRet;
        }

        // book
        insertResStmt.clearParameters();
        int resId = getLastResId() + 1;
        insertResStmt.setInt(1, resId);  // rid
        insertResStmt.setInt(2, itineraries.get(itineraryId).f1.fid);  // f1id
        Flight f2 = itineraries.get(itineraryId).f2;
        if (f2 != null) { 
          insertResStmt.setInt(3, f2.fid);
        } else {
          insertResStmt.setNull(3, java.sql.Types.INTEGER);  // f2id
        }
        insertResStmt.setString(4, currUser); // username
        insertResStmt.setInt(5, 0);  // isPaidFor (false)
        insertResStmt.executeUpdate();

        conn.commit();
        setAutoCommit(conn, true);
        return "Booked flight(s), reservation ID: " + resId + "\n";

      } catch(SQLException e) {
        rollback(conn);
        setAutoCommit(conn, true);
        if (!isDeadlock(e)) {
          e.printStackTrace();
          return failRet;
        }
      }
    }
    return failRet;
  }

  // Returns true if currUser already booked reservation on dayOfMonth, false otherwise
  private boolean alreadyBookedOn(int dayOfMonth) throws SQLException {
    numBookedOnStmt.clearParameters();
    numBookedOnStmt.setInt(1, dayOfMonth);
    numBookedOnStmt.setString(2, currUser);
    ResultSet numBookedOnRs = numBookedOnStmt.executeQuery();
    numBookedOnRs.next();

    boolean alreadyBooked = (numBookedOnRs.getInt("count") > 0);
    numBookedOnRs.close();
    return alreadyBooked;
  }

  // Returns last reservation ID booked (i.e., num reservations)
  private int getLastResId() throws SQLException {
    ResultSet numResRs = numResStmt.executeQuery();
    numResRs.next();
    int lastResId = numResRs.getInt("count");
    numResRs.close();
    return lastResId;
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_pay(int reservationId) {
    // TODO: YOUR CODE HERE
    if (currUser == null) return "Cannot pay, not logged in\n";
    final String failRet = "Failed to pay for reservation " + reservationId + "\n";
    try {
      // more error checking (that does sql)
      final String cantFind = "Cannot find unpaid reservation " + reservationId + " under user: " + currUser + "\n";
      // get res details
      getResStmt.clearParameters();
      getResStmt.setInt(1, reservationId);
      setAutoCommit(conn, false);
      ResultSet getResRs = getResStmt.executeQuery();
      if (!getResRs.next()) {
        rollback(conn);
        setAutoCommit(conn, true);
        getResRs.close();
        return cantFind;
      }
      String resUser = getResRs.getString("username");
      int isPaidFor = getResRs.getInt("isPaidFor");
      if (!resUser.equals(currUser) || isPaidFor == 1) {
        rollback(conn);
        setAutoCommit(conn, true);
        getResRs.close();
        return cantFind;
      }
      // get itinerary price
      // f1 price
      int f1id = getResRs.getInt("f1id");
      flightPriceStmt.clearParameters();
      flightPriceStmt.setInt(1, f1id);
      ResultSet f1PriceRs = flightPriceStmt.executeQuery();
      f1PriceRs.next();
      int itinPrice = f1PriceRs.getInt("price");
      f1PriceRs.close();
      // f2 price
      int f2id = getResRs.getInt("f2id");
      if (!getResRs.wasNull()) {
        flightPriceStmt.clearParameters();
        flightPriceStmt.setInt(1, f2id);
        ResultSet f2PriceRs = flightPriceStmt.executeQuery();
        f2PriceRs.next();
        itinPrice += f2PriceRs.getInt("price");
        f2PriceRs.close();
      }
      getResRs.close();
      // get user balance
      getUserStmt.clearParameters();
      getUserStmt.setString(1, currUser);
      ResultSet getUserRs = getUserStmt.executeQuery();
      getUserRs.next();
      int balance = getUserRs.getInt("balance");
      getUserRs.close();
      if (balance < itinPrice) {
        rollback(conn);
        setAutoCommit(conn, true);
        return "User has only " + balance + " in account but itinerary costs " + itinPrice + "\n";
      }

      // pay
      payResStmt.clearParameters();
      payResStmt.setInt(1, reservationId);
      payResStmt.executeUpdate();

      lowerBalStmt.clearParameters();
      lowerBalStmt.setInt(1, itinPrice);
      lowerBalStmt.setString(2, currUser);
      lowerBalStmt.executeUpdate();

      conn.commit();
      setAutoCommit(conn, true);
      return "Paid reservation: " + reservationId + " remaining balance: " + (balance - itinPrice) + "\n";

    } catch(SQLException e) {
      rollback(conn);
      setAutoCommit(conn, true);
      e.printStackTrace();
      return failRet;
    }
  }

  /* See QueryAbstract.java for javadoc */
  public String transaction_reservations() {
    // TODO: YOUR CODE HERE
    if (currUser == null) return "Cannot view reservations, not logged in\n";
    try {
      // more error checking (that does sql)
      getResForStmt.clearParameters();
      getResForStmt.setString(1, currUser);
      setAutoCommit(conn, false);
      ResultSet getResForRs = getResForStmt.executeQuery();
      if (!getResForRs.next()) {
        rollback(conn);
        setAutoCommit(conn, true);
        getResForRs.close();
        return "No reservations found\n";
      }

      // make ret string
      StringBuffer sb = new StringBuffer();

      while (true) {  // check condition for first iteration above, condition for rest of iterations at end of loop
        sb.append("Reservation " + getResForRs.getInt("rid") + " paid: " + (getResForRs.getInt("isPaidFor") == 1) + ":\n");

        int f1id = getResForRs.getInt("f1id");
        Flight f1 = new Flight(f1id);
        sb.append(f1.toString() + "\n");

        int f2id = getResForRs.getInt("f2id");
        if (!getResForRs.wasNull()) {
          Flight f2 = new Flight(f2id);
          sb.append(f2.toString() + "\n");
        }

        if (!getResForRs.next()) break;
      }
      getResForRs.close();
      conn.commit();
      setAutoCommit(conn, true);
      return sb.toString();

    } catch(SQLException e) {
      rollback(conn);
      setAutoCommit(conn, true);
      e.printStackTrace();
      return "Failed to retrieve reservations\n";
    }
  }

  /**
   * Example utility function that uses prepared statements
   */
  /**
    private int getFlightCapacity(int fid) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setInt(1, fid);

    ResultSet results = flightCapacityStmt.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  } */

  /**
   * Utility function to determine whether an error was caused by a deadlock
   */
  private static boolean isDeadlock(SQLException e) {
    return "40001".equals(e.getSQLState()) || "40P01".equals(e.getSQLState());
  }

  /**
   * A class to store information about a single Itinerary 
   */
  class Itinerary implements Comparable<Itinerary> {
    // Itinerary's flights
    // If no 2nd flight, f2 will be null
    public Flight f1, f2;

    // Time for flight 1 + time for flight 2
    public int totalTime;

    // Constructs a direct itinerary given details for single flight
    public Itinerary(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
                     int cap, int pri) {
      f1 = new Flight(id, day, carrier, fnum, origin, dest, tm, cap, pri);
      f2 = null;
      totalTime = tm;
    }

    // Constructs an indirect itinerary given details for both flights
    public Itinerary(int f1Id, int f1Day, String f1Carrier, String f1FNum,
                     String f1Origin, String f1Dest, int f1Tm, int f1Cap, int f1Pri,
                     int f2Id, int f2Day, String f2Carrier, String f2FNum,
                     String f2Origin, String f2Dest, int f2Tm, int f2Cap, int f2Pri) { 
      f1 = new Flight(f1Id, f1Day, f1Carrier, f1FNum, f1Origin, f1Dest, f1Tm, f1Cap, f1Pri);
      f2 = new Flight(f2Id, f2Day, f2Carrier, f2FNum, f2Origin, f2Dest, f2Tm, f2Cap, f2Pri);
      totalTime = f1Tm + f2Tm;
    }

    // Returns negative if this itinerary's total time is smaller than other's, positive if vice versa
    // If total times are tied,
    // returns negative if this itinerary's 1st flight's id is smaller than other's, positive if vice versa
    // If 1st flights' ids are tied (so both itineraries are indirect),
    // returns negative if this itinerary's 2nd flight's id is smaller than other's, positive if vice versa
    public int compareTo(Itinerary o) {
      if (this.totalTime != o.totalTime) {
        return this.totalTime - o.totalTime;
      } else if (this.f2 != null && o.f2 != null) { // indirect
        if (this.f1.fid != o.f1.fid) {
          return this.f1.fid - o.f1.fid;
        } else {
          return this.f2.fid - o.f2.fid;
        }
      } else {
        return this.f1.fid - o.f1.fid;
      }
    }

    // Returns true if itinerary has space on all of its flights, otherwise false
    public boolean hasSpace() throws SQLException {
      numReservedOnF1Stmt.clearParameters();
      numReservedOnF1Stmt.setInt(1, this.f1.fid);
      ResultSet numReservedOnF1Rs = numReservedOnF1Stmt.executeQuery();
      numReservedOnF1Rs.next();
      int numReservedOnF1 = numReservedOnF1Rs.getInt("count");
      numReservedOnF1Rs.close();
      if (f2 == null) {
        return numReservedOnF1 < this.f1.capacity;
      } else {
        numReservedOnF2Stmt.clearParameters();
        numReservedOnF2Stmt.setInt(1, this.f2.fid);
        ResultSet numReservedOnF2Rs = numReservedOnF2Stmt.executeQuery();
        numReservedOnF2Rs.next();
        int numReservedOnF2 = numReservedOnF2Rs.getInt("count");
        numReservedOnF2Rs.close();
        return numReservedOnF1 < f1.capacity && numReservedOnF2 < f2.capacity;
      }
    }
  }

  /**
   * A class to store information about a single flight
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    // Given flight id, retrieves rest of flight information, constructs new Flight
    // Throws IllegalArgumentException if no flight with id found
    Flight(int id) throws SQLException {
      getFlightStmt.clearParameters();
      getFlightStmt.setInt(1, id);
      ResultSet flightRs = getFlightStmt.executeQuery();
      if (!flightRs.next()) throw new IllegalArgumentException("No flight with id " + id + " found\n");

      fid = flightRs.getInt("fid");
      dayOfMonth = flightRs.getInt("day_of_month");
      carrierId = flightRs.getString("cid");
      flightNum = flightRs.getInt("op_carrier_flight_num") + "";
      originCity = flightRs.getString("origin_city");
      destCity = flightRs.getString("dest_city");
      time = flightRs.getInt("duration_mins");
      capacity = getCapacity(flightRs.getString("tail_num"));
      price = flightRs.getInt("price");

      flightRs.close();
    }

    Flight(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
           int cap, int pri) {
      fid = id;
      dayOfMonth = day;
      carrierId = carrier;
      flightNum = fnum;
      originCity = origin;
      destCity = dest;
      time = tm;
      capacity = cap;
      price = pri;
    }
    
    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
          + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
          + " Capacity: " + capacity + " Price: " + price;
    }
  }
}
