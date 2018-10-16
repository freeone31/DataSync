package DataSyncApp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс точки входа в приложение. Приложение экспортирует данные из БД в файл или синхронизирует данные БД в соответствии с файлом.
 */
public class DataSync {
    /**
     * Логгер приложения; логи выводятся в консоль и в файл. В консоль - пользовательские, основные (info и выше), в файл - для отладки, подробные (debug и выше).
     * имя файла задается в log4j2.properties.
     * Для более подробной отладки переключить фильтр вывода в файл на Trace.
     */
    static Logger logger;

    /**
     * Вызываемый метод приложения. Принимает на вход название комманды (export/sync) и имя файла с расширением xml.
     * @param args параметры командной строки
     */
    public static void main(String[] args) {
        // комманда (export/sync) и имя файла
        String mCommand, mFileName;

        try {
            logger = LogManager.getLogger(DataSync.class.getPackage().getName());

            logger.info("Welcome to DataSyncApp!");
            logger.debug("args.length = " + args.length);

            if (args.length != 2) {
                logger.info("Enter command and filename: 'DataSync.bat [export/sync] [filename]'");
                throw new SoftException();
            }

            mCommand = args[0];
            mFileName = args[1];

            logger.trace("mCommand = " + mCommand);
            logger.trace("mFileName = " + mFileName);

            if (!mCommand.equals("export") && !mCommand.equals("sync")) {
                logger.info("Incorrect command: choose 'export' or 'sync'");
                throw new SoftException();
            }

            Commander commander = new Commander(mFileName);

            if (mCommand.equals("export")) {
                logger.info("Starting export from DB to file");
                commander.export();

            } else if (mCommand.equals("sync")) {
                logger.info("Starting synchronization of file and DB");
                commander.sync();
            }
        } catch (SoftException sofEx) {
            logger.debug("Caught soft exception in Main block");
            logger.debug(sofEx);
            logger.info("Exiting application");

            // штатный выход, без ошибок: например, если пользователю было предложено перезаписать имеющийся файл, и он отказался

        } catch (Exception ex) {
            logger.debug("Caught exception in Main block");
            logger.error(ex);
            logger.error("An error occured, exiting application");
            System.exit(1);
        }
    }
}
