package DataSyncApp;

import java.io.*;
import java.sql.*;
import java.util.*;

import static DataSyncApp.DataSync.logger;

/**
 * Класс отвечает за взаимодействие с БД: осуществляет подключениек БД, загрузку данных из мапа в БД, выгрузку данных в мап из БД.
 */
class DbController {
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    /**
     * Создание контроллера БД. При создании автоматически определяются свойства подключения из файла свойств.
     * Свойства задаются в dbconnection.properties.
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    DbController() throws HardException {
        Properties dbConnProps;
        // проверяем наличие файла свойств подключения, и всех необходимых параметров
        try {
            logger.debug("Creating new DbController");

            dbConnProps = new Properties();
            dbConnProps.load(new FileInputStream("src/main/resources/dbconnection.properties"));
            logger.debug("properties loaded");

        } catch (IOException ioEx) {
            logger.error("Error: can't reach file with DB connection properties!");
            throw new HardException(ioEx);
        }

        if (!dbConnProps.containsKey("dbUrl")) {
            logger.error("Error: file with DB connection properties doesn't contain connection URL!");
            throw new HardException();
        }

        if (!dbConnProps.containsKey("dbUsername")) {
            logger.error("Error: file with DB connection properties doesn't contain username!");
            throw new HardException();
        }

        if (!dbConnProps.containsKey("dbPassword")) {
            logger.error("Error: file with DB connection properties doesn't contain user password!");
            throw new HardException();
        }

        dbUrl = dbConnProps.getProperty("dbUrl");
        logger.debug("dbUrl = " + dbUrl);

        dbUsername = dbConnProps.getProperty("dbUsername");
        logger.debug("dbUsername = " + dbUsername);

        dbPassword = dbConnProps.getProperty("dbPassword");
        logger.debug("dbPassword = " + dbPassword);

        try {
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            DriverManager.setLoginTimeout(10);
        } catch (SQLException sqlEx) {
            logger.error("Error: can't register driver for DB connection!");
            throw new HardException(sqlEx);
        }
    }

    /**
     * Подключение к БД и получение данных из нее в мап.
     * @return мап-коллекция (Map({@link Pair}, {@link Dep})), в которой ключом служит экземпляр Pair (пара DepCode и DepJob), а значением - экземпляр Dep (вся строка таблицы, кроме id)
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    Map<Pair,Dep> getDataFromDb() throws HardException {
        logger.info("Getting data from DB");
        Map<Pair,Dep> oMap = new HashMap<>();

        // подключение к БД
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            logger.debug("conn = " + conn);
            logger.trace("Checking connection");

            // проверка успешности подключения
            if (conn == null) {
                logger.error("Error: Invalid connection");
                throw new HardException();
            }
            logger.trace("Valid connection");

            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT DEPCODE, DEPJOB, DESCRIPTION FROM DZ_COMPANY")) {
                while (rs.next()) {
                    Pair pair = new Pair((String) rs.getObject("DEPCODE"), (String) rs.getObject("DEPJOB"));
                    Dep dep = new Dep(pair, (String) rs.getObject("DESCRIPTION"));
                    logger.trace("dep = " + dep.toString());
                    // формируем выходной мап
                    oMap.put(pair, dep);
                }
            } catch (SQLException sqlEx) {
                logger.error("Error: can't get data from DB");
                throw new HardException(sqlEx);
            }
        } catch (SQLException sqlEx) {
            logger.error("Error: can't connect to DB");
            throw new HardException(sqlEx);
        } catch(Exception ex) {
            logger.error("Error: failed to get data from DB");
            throw new HardException(ex);
        }

        return oMap;
    }

    /**
     * Подключение к БД и изменение данных в ней в соответствии со входными списками удаления, изменения, добавления строк.
     * @param iMap, в котором ключом служит строка-обозначение переданного сета (deleteSet, updateSet, insertSet),
     * а значением - один из трех хэшсетов, в которых соответственно перечислены элементы (Dep) для удаления, обновления и вставки в бд.
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    void writeDataToDb(Map<String,Set<Dep>> iMap) throws HardException {
        logger.info("Writing data to DB");
        logger.trace("iMap = " + iMap.toString());

        // подключение к БД
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            logger.debug("conn = " + conn);
            logger.trace("Checking connection");

            // проверка успешности подключения
            if (conn == null) {
                logger.error("Error: Invalid connection");
                throw new HardException();
            }
            logger.trace("Valid connection");

            // выключаем автокоммит и создаем точку сохранения
            conn.setAutoCommit(false);
            Savepoint sv = conn.setSavepoint();

            try (Statement stmt = conn.createStatement()) {
                final int BATCH_SIZE = 5; // размер пачки команд, которая будет передаваться в БД
                int numRows = 0; // счетчик команд

                // команды для удаления строк
                for (Dep delDep : iMap.get("deleteSet")) {
                    Pair delPair = delDep.getPair();
                    logger.trace("delPair = " + delPair.toString());

                    String sql = "DELETE FROM DZ_COMPANY WHERE (DEPCODE = '" + delPair.getCode() + "' AND DEPJOB = '" + delPair.getJob() + "')";
                    logger.trace("sql = " + sql);
                    stmt.addBatch(sql);

                    if (++numRows % BATCH_SIZE == 0) {
                        logger.debug("executing (numRows = " + numRows + ")");
                        stmt.executeBatch();
                    }
                }

                // команды для апдейта строк
                for (Dep updDep : iMap.get("updateSet")) {
                    logger.trace("updDep = " + updDep.toString());
                    Pair updPair = updDep.getPair();
                    String desc;

                    if (updDep.getDescription() == null) {
                        desc = "NULL";
                    } else if (updDep.getDescription().equals("")) {
                        desc = "''";
                    } else {
                        desc = "'" + updDep.getDescription() + "'";
                    }

                    String sql = "UPDATE DZ_COMPANY SET DESCRIPTION = " + desc + " WHERE (DEPCODE = '" + updPair.getCode() + "' AND DEPJOB = '" + updPair.getJob() + "')";
                    logger.trace("sql = " + sql);
                    stmt.addBatch(sql);

                    if (++numRows % BATCH_SIZE == 0) {
                        logger.debug("executing (numRows = " + numRows + ")");
                        stmt.executeBatch();
                    }
                }

                // команды для вставки строк
                for (Dep insDep : iMap.get("insertSet")) {
                    logger.trace("insDep = " + insDep.toString());
                    Pair insPair = insDep.getPair();
                    String desc;

                    if (insDep.getDescription() == null) {
                        desc = "NULL";
                    } else if (insDep.getDescription().equals("")) {
                        desc = "''";
                    } else {
                        desc = "'" + insDep.getDescription() + "'";
                    }

                    String sql = "INSERT INTO DZ_COMPANY (DEPCODE, DEPJOB, DESCRIPTION) VALUES ('" + insPair.getCode() + "', '" + insPair.getJob() + "', " + desc + ")";
                    logger.trace("sql = " + sql);
                    stmt.addBatch(sql);

                    if (++numRows % BATCH_SIZE == 0) {
                        logger.debug("executing (numRows = " + numRows + ")");
                        stmt.executeBatch();
                    }
                }

                logger.debug("last executing (numRows = " + numRows + ")");
                stmt.executeBatch();
                logger.debug("executed");

                // сохраняем
                conn.commit();
                logger.debug("commited");

            } catch (Exception ex) {
                // откатываемся в случае ошибки
                conn.rollback(sv);
                logger.error("Error: unable to make changes in DB");
                throw new HardException(ex);
            }
        } catch (SQLException sqlEx) {
            logger.error("Error: can't connect to DB");
            throw new HardException(sqlEx);
        } catch (Exception ex) {
            logger.error("Error: failed to write data to DB");
            throw new HardException(ex);
        }
    }
}
