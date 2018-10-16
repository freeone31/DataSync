package DataSyncApp;

import java.util.*;
import static DataSyncApp.DataSync.logger;

/**
 * Осуществляет взаимодействие с бд и файлом по команде пользователя. Раздает команды на выгрузку/загрузку в БД/файл.
 * Обмен данными между БД и файлом происходит посредством мап-коллекций Map({@link Pair}, {@link Dep}).
 */
class Commander {
    private DbController mDbController;     // контроллер БД
    private FileController mFileController; // контроллер файла

    /**
     * Создание коммандера. По имени файла создается контроллер файла.
     * @param iFilename имя файла
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    Commander(String iFilename) throws HardException {
        try {
            logger.debug("Creating new Commander; iFilename = " + iFilename);

            mDbController = new DbController();
            logger.trace("mDbController created");

            mFileController = new FileController(iFilename);
            logger.trace("mFileController created");

        } catch(Exception ex) {
            logger.error("Error: unable to start core element");
            throw new HardException(ex);
        }
    }

    /**
     * Осуществляет обработку команды на экспорт данных из БД в файл. Если в БД ничего нет - создаст пустой файл.
     * @throws SoftException контролируемый выход из приложения
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    void export() throws SoftException, HardException {
        try {
            logger.info("Running export from DB to file");

            // получение данных из БД
            Map<Pair, Dep> dbMap = mDbController.getDataFromDb();

            logger.trace("dbMap.size() = " + dbMap.size());

            // если в БД пусто, создаем пустой файл
            if (dbMap.size() == 0) {
                logger.info("No data found in DB; empty file will be created");
            }
            else {
                logger.info("Successfully got data from DB; going to write data to file");
            }

            // проверяем возможность записи данных в файл
            mFileController.checkFileOnWrite();
            logger.debug("File successfully checked before write");

            // запись данных в файл
            mFileController.writeDataToFile(dbMap);
            logger.info("Successfully wrote data to file");

        } catch(SoftException sofEx) {
            throw new SoftException(sofEx);
        } catch(Exception ex) {
            logger.error("Error: failed to export data from DB to file");
            throw new HardException(ex);
        }
    }

    /**
     * Осуществляет обработку команды на синхронизацию данных в БД в соответствии с файлом.
     * Если файл пустой - спросит у пользователя, точно ли он хочет обновить БД в соответствии с файлом (т.е. удалить все данные);
     * далее в зависимости от ответа пользователя либо удаляет, либо завершает работу.
     * @throws SoftException контролируемый выход из приложения
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    void sync() throws SoftException, HardException {
        try {
            logger.info("Running synchronization of file and DB");

            // получение данных из БД. Если в БД пусто - не предупреждаем, для синхронизации это нормально
            Map<Pair, Dep> dbMap = mDbController.getDataFromDb();
            logger.info("Successfully got data from DB; going to get data from file");
            logger.trace("dbMap.size() = " + dbMap.size());

            // проверяем возможность получения данных из файла
            mFileController.checkFileOnRead();
            logger.debug("File successfully checked before read");

            // получаем данные из файла
            Map<Pair, Dep> fileMap = mFileController.getDataFromFile();
            logger.trace("fileMap.size() = " + fileMap.size());

            // Если файл был пустой, то в соответствии с ним придется стереть все данные из БД; здесь спросим у пользователя, точно ли это то, что ему нужно.
            checkZeroMapSize(fileMap.size());

            // расчитываем различия между файлом и БД, составляем три списка - какие строки удалить из БД, какие изменить, какие добавить.
            // все три списка (Set<Dep>) выгрузим одним мапом; ключом будет предназначение сета.
            Map<String,Set<Dep>> mergedMap = mergeMaps(fileMap, dbMap);
            logger.info("Successfully calculated changes; going to write new data to DB");

            // обновляем БД в соответствии с файлом, точнее - с полученными выше списками-сетами.
            mDbController.writeDataToDb(mergedMap);
            logger.info("Successfully wrote new data to DB");

        } catch(SoftException sofEx) {
            throw new SoftException(sofEx);
        } catch(Exception ex) {
            logger.error("Error: failed to synchronize file and DB data");
            throw new HardException(ex);
        }
    }

    /**
     * Если файл был пустой, то в соответствии с ним придется стереть все данные из БД; здесь спросим у пользователя, точно ли это то, что ему нужно.
     * В зависимости от ответа пользователя либо выходим, либо идем дальше.
     * @param mapSize размер мапа файла
     * @throws SoftException контролируемый выход из приложения
     */
    private void checkZeroMapSize(int mapSize) throws SoftException {
        // если размер мапа нулевой, значит файл совсем пустой. Если файла нет, если он кривой,
        // если в нем некорректные данные, и прочие проверки происходят на этапе получения данных.
        if (mapSize == 0) {
            logger.info("No data found in file; if proceed, ALL data from DB will be deleted; continue? (y - delete, n - exit application)");
            String inputString;

            try (Scanner sc = new Scanner(System.in)) {
                inputString = sc.nextLine();
                logger.trace("inputString = " + inputString);

                // мучаем пользователя, пока он не введет один из двух допустимых ответов
                while (!inputString.equals("y") && !inputString.equals("n")) {
                    logger.info("Incorrect input. Type 'y' if you want to delete all data in DB, or 'n' if you want to exit application");
                    inputString = sc.nextLine();
                    logger.trace("inputString = " + inputString);
                }
            }

            // пробрасываем наверх желание выйти
            if (inputString.equals("n")) {
                logger.info("You chose 'n'; exiting application");
                throw new SoftException();
            }
            else {
                // пользователь согласен снести БД пустым файлом
                logger.info("You chose 'y'; all data from DB will be deleted; going to calculate changes");
            }
        }
        else {
            logger.info("Successfully got data from file; going to calculate changes");
        }
    }

    /**
     * Имея на руках данные из файла и БД (Map({@link Pair}, {@link Dep})), формирует три списка - для удаления из БД, для изменения и для инсерта в БД.
     * Сравниваем мапы. Весь больший мап сразу целиком помечаем, как будто его элементов нет в меньшем мапе. Это значит, что если бежим по файлу,
     * то считаем, что весь мап БД нужно удалить. Если бежим по мапу БД, то считаем, что весь мап файла нужно инсертить.
     * Пробегаем по меньшему из мапов, для небольшого ускорения. По ключу ищем элементы меньшего мапа в большем. Если нашлось совпадение элемента по ключу,
     * смотрим, совпадают ли они целиком. Если совпадают, то элемент остался без изменения, если не совпадают - элемент нужно апдейтить,
     * заносим элемент мапа файла в список апдейта.
     * Если совпадения по ключу не нашлось - элемента одного из мапов нет в другом мапе, то: если бежим по мапу файла, заносим этот элемент в список инсерта,
     * а если бежим по мапу БД, заносим этот элемент в список удаления.
     * Все три списка формируются за один проход. Компонуем их в мап, ключом будет предназначение списка (deleteSet, updateSet, insertSet).
     * @param iFileMap мап содержиого файла Map(Pair, Dep)
     * @param iDbMap мап содержимого БД Map(Pair, Dep)
     * @return мап, в котором ключом служит строка-обозначение переданного сета (deleteSet, updateSet, insertSet),
     * а значением - один из трех хэшсетов, в которых соответственно перечислены элементы (Dep) для удаления, обновления и вставки в бд.
     * @throws SoftException контролируемый выход из приложения
     */
    private Map<String,Set<Dep>> mergeMaps(Map<Pair,Dep> iFileMap, Map<Pair,Dep> iDbMap) throws SoftException {
        logger.debug("mergeMaps started");
        logger.trace("iFileMap = " + iFileMap.toString());
        logger.trace("iDbMap = " + iDbMap.toString());
        logger.debug("iFileMap.size() = " + iFileMap.size());
        logger.debug("iDbMap.size() = " + iDbMap.size());

        // выходной мап "предназначение-список элементов"
        Map<String,Set<Dep>> oMap = new HashMap<>();
        Set<Dep> mDeleteSet; // список элементов для удаления (набор элементов из мапа БД, которых нет в мапе файла)
        Set<Dep> mInsertSet; // список элементов для вставки (набор элементов из мапа файла, которых нет в мапе БД)
        Set<Dep> mEditedSet = new HashSet<>(); // список элементов для апдейта (набор элементов которые есть в обоих мапах (совпали по ключу), но отличаются другими полями, то есть измененные)

        // если мап файла меньше, бежать будем по нему
        if (iFileMap.size() < iDbMap.size()) {
            logger.trace("run on iFileMap");
            // сразу считаем, что всех элементов мапа БД нет в мапе файла, пока не убедимся в обратном (после чего удаляем элемент из списка)
            mDeleteSet = new HashSet<>(iDbMap.values());
            mInsertSet = new HashSet<>();

            // бежим по мапу файла
            for (Map.Entry<Pair, Dep> fileEntry : iFileMap.entrySet()) {
                Pair mFilePair = fileEntry.getKey();
                Dep mFileDep = fileEntry.getValue();
                logger.trace("mFileDep = " + mFileDep.toString());

                // смотрим, есть ли в мапе БД элемент таким же ключом
                if (iDbMap.containsKey(mFilePair)) {
                    Dep mDbDep = iDbMap.get(mFilePair);
                    logger.debug("mDbDep = " + mDbDep.toString());
                    // совпадение для элемента нашлось, удаляем из списка отсутствующих
                    logger.trace("Equal Pairs; Removing mDbDep from mDeleteSet");
                    mDeleteSet.remove(mDbDep);

                    // смотрим, совпадают ли они целиком. Если целиком совпадают, то ничего не делаем
                    if (!mDbDep.equals(mFileDep)) {
                        logger.trace("Not equal Deps with equal Pairs; Adding mFileDep to mEditedSet");
                        // целиком не совпадают - то есть элемент изменился
                        mEditedSet.add(mFileDep);
                    }
                }
                else {
                    // в мапе БД такого элемента нет, помечаем
                    logger.trace("Adding mFileDep to mInsertSet");
                    mInsertSet.add(mFileDep);
                }
            }
        }
        else {
            // если мап БД меньше, бежать будем по нему
            logger.trace("run on iDbMap");
            // сразу считаем, что всех элементов мапа файла нет в мапе БД, пока не убедимся в обратном (после чего удаляем элемент из списка)
            mInsertSet = new HashSet<>(iFileMap.values());
            mDeleteSet = new HashSet<>();

            // бежим по мапу БД
            for (Map.Entry<Pair, Dep> dbEntry : iDbMap.entrySet()) {
                Pair mDbPair = dbEntry.getKey();
                Dep mDbDep = dbEntry.getValue();
                logger.trace("mDbDep = " + mDbDep.toString());

                // смотрим, есть ли в мапе файла элемент таким же ключом
                if (iFileMap.containsKey(mDbPair)) {
                    Dep mFileDep = iFileMap.get(mDbPair);
                    logger.debug("mFileDep = " + mFileDep.toString());
                    // совпадение для элемента нашлось, удаляем из списка отсутствующих
                    logger.trace("Equal Pairs; Removing mFileDep from mInsertSet");
                    mInsertSet.remove(mFileDep);

                    // смотрим, совпадают ли они целиком. Если целиком совпадают, то ничего не делаем
                    if (!mFileDep.equals(mDbDep)) {
                        logger.trace("Not equal Deps with equal Pairs; Adding mFileDep to mEditedSet");
                        // целиком не совпадают - то есть элемент изменился
                        mEditedSet.add(mFileDep);
                    }
                }
                else {
                    // в мапе файла такого элемента нет, помечаем
                    logger.trace("Adding mDbDep to mDeleteSet");
                    mDeleteSet.add(mDbDep);
                }
            }
        }

        // если все три списка пустые, значит файл и БД совпадают - выходим
        if (mDeleteSet.size() == 0 && mInsertSet.size() == 0 && mEditedSet.size() == 0) {
            logger.info("File and DB are identical, no changes required");
            throw new SoftException();
        }

        // выходной мап
        oMap.put("deleteSet", mDeleteSet);
        oMap.put("updateSet", mEditedSet);
        oMap.put("insertSet", mInsertSet);
        logger.debug("oMap = " + oMap.toString());

        return oMap;
    }
}
