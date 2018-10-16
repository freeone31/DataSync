package DataSyncApp;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import static DataSyncApp.DataSync.logger;

/**
 * Класс отвечает за взаимодействие с файлом: осуществляет открытие файла, загрузку данных из мапа в файл, выгрузку данных в мап из файла.
 */
class FileController {
    private String mFilename; // имя файла
    private File mFile;       // файл, из которого будем брать или в который будем записывать данные

    /**
     * создание контроллера файла.
     * @param filename имя файла
     */
    FileController(String filename) {
        logger.debug("Creating new FileController; mFilename = " + filename);
        mFilename = filename;
    }

    /**
     * Проверка файла перед попыткой чтения из него данных. Если будут ошибки, ругнется. Если нет, в mFile будет читаемый файл.
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    void checkFileOnRead() throws HardException {
        logger.debug("Checking file before read");

        try {
            mFile = new File(mFilename);

            // блок проверок файла
            logger.trace("mFile.exists() = " + mFile.exists());

            if(!mFile.exists()) {
                logger.error("Error: file not found");
                throw new HardException();
            }

            logger.trace("mFile.isDirectory() = " + mFile.isDirectory());

            if(mFile.isDirectory()) {
                logger.error("Error: '" + mFilename + "' is directory");
                throw new HardException();
            }

            logger.trace("mFilename.endsWith(\".xml\") = " + mFilename.endsWith(".xml"));

            if(!mFilename.endsWith(".xml")) {
                logger.error("Error: file must have .xml extension");
                throw new HardException();
            }

            logger.trace("mFile.canRead() = " + mFile.canRead());

            if(!mFile.canRead()) {
                logger.error("Error: Unable to read file");
                throw new HardException();
            }
        } catch (Exception ex) {
            logger.error("Error: failed to get data from file");
            throw new HardException(ex);
        }
    }

    /**
     * Открытие файла и получение данных из него в мап. Ругнется, если найдет в файле две и более сущности с одинаковым ключом DepCode-DepJob.
     * @return мап-коллекция (Map({@link Pair}, {@link Dep})), в которой ключом служит экземпляр Pair (пара полей DepCode и DepJob), а значением - экземпляр Dep (вся поля одной сущности)
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    Map<Pair,Dep> getDataFromFile() throws HardException {
        logger.info("Getting data from file");
        Map<Pair,Dep> oMap = new HashMap<>(); // выходной мап

        try {
            logger.trace("mFile.length() = " + mFile.length());

            if (mFile.length() == 0) {
                logger.info("File is empty");
                return oMap;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(mFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Row");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    // проверяем, чтобы оба обязательных поля ключа были, при чем не пустые
                    if (eElement.getElementsByTagName("DEPCODE").getLength() == 0) {
                        logger.error("Error: Mandatory field DepCode is not found in node " + i + "!");
                        throw new HardException();
                    }

                    if (eElement.getElementsByTagName("DEPJOB").getLength() == 0) {
                        logger.error("Error: Mandatory field DepJob is not found in node " + i + "!");
                        throw new HardException();
                    }

                    if (eElement.getElementsByTagName("DEPCODE").item(0).getTextContent().equals("")) {
                        logger.error("Error: Mandatory field DepCode is empty in node " + i + "!");
                        throw new HardException();
                    }

                    if (eElement.getElementsByTagName("DEPJOB").item(0).getTextContent().equals("")) {
                        logger.error("Error: Mandatory field DepJob is empty in node " + i + "!");
                        throw new HardException();
                    }

                    // преобразуем сущности из xml в элементы Dep с ключом Pair
                    Pair newPair = new Pair(eElement.getElementsByTagName("DEPCODE").item(0).getTextContent(),
                                            eElement.getElementsByTagName("DEPJOB").item(0).getTextContent());

                    logger.trace("newPair = " + newPair.toString());
                    logger.trace("oMap.containsKey(newPair) = " + oMap.containsKey(newPair));

                    // проверка на уникальность ключа
                    if (oMap.containsKey(newPair)) {
                        logger.error("Error: File contains multiple rows with the same key!");
                        throw new HardException();
                    }

                    Dep newDep; // элемент выходного мапа

                    if (eElement.getElementsByTagName("DESCRIPTION").getLength() == 1) {
                        // если описание задано (даже пустой строкой)
                        newDep = new Dep(newPair, eElement.getElementsByTagName("DESCRIPTION").item(0).getTextContent());
                    }
                    else {
                        // если записи нет, значит там был нулл - пишем нулл
                        newDep = new Dep(newPair, null);
                    }
                    logger.trace("newDep = " + newDep.toString());

                    oMap.put(newPair, newDep);
                }
            }
        } catch (IOException ioEx) {
            logger.error("Error: failed to read data from file");
            throw new HardException(ioEx);
        } catch (SAXParseException saxEx) {
            logger.error("Error: inappropriate file content");
            throw new HardException(saxEx);
        } catch (Exception ex) {
            logger.error("Error: failed to get data from file");
            throw new HardException(ex);
        }

        return oMap;
    }

    /**
     * Проверка файла перед попыткой записи в него данных. Если будут ошибки, ругнется. При перезаписи спросит решения пользователя.
     * Если все ок, в mFile будет записываемый файл.
     * @throws SoftException контролируемый выход из приложения
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    void checkFileOnWrite() throws SoftException, HardException {
        logger.debug("Checking file before write");

        mFile = new File(mFilename);

        // блок проверок файла
        logger.trace("mFile.isDirectory() = " + mFile.isDirectory());

        if(mFile.isDirectory()) {
            logger.error("Error: '" + mFilename + "' is directory");
            throw new HardException();
        }

        logger.trace("mFilename.endsWith(\".xml\") = " + mFilename.endsWith(".xml"));

        if(!mFilename.endsWith(".xml")) {
            logger.error("Error: file must have .xml extension");
            throw new HardException();
        }

        logger.trace("mFile.exists() = " + mFile.exists());

        if(mFile.exists()) {
            logger.trace("mFile.canWrite() = " + mFile.canWrite());

            if(!mFile.canWrite()) {
                logger.error("Error: Unable to write file");
                throw new HardException();
            }

            logger.info("File already exists. Do you want to overwrite it? (y - overwrite, n - exit application)");

            String inputString;

            try (Scanner sc = new Scanner(System.in)) {
                inputString = sc.nextLine();
                logger.trace("inputString = " + inputString);

                // заставляем пользователя сказать, перезаписываем файл или выходим
                while (!inputString.equals("y") && !inputString.equals("n")) {
                    logger.info("Incorrect input. Type 'y' if you want to overwrite file, or 'n' if you want to exit application");
                    inputString = sc.nextLine();
                    logger.trace("inputString = " + inputString);
                }
            }

            if (inputString.equals("n")) {
                logger.info("You chose 'n'; exiting application");
                throw new SoftException();
            }
            else {
                logger.info("You chose 'y'; file will be overwrited");
            }
        }
    }

    /**
     * Запись данных из мапа (Map({@link Pair}, {@link Dep})) в файл. При загрузке в файл, если файл уже существует, спросит у пользователя, желает ли он его перезаписать.
     * @param iMap мап-коллекция (Map({@link Pair}, {@link Dep})), в которой ключом служит экземпляр Pair (пара DepCode и DepJob), а значением - экземпляр Dep (вся строка таблицы, кроме id)
     * @throws HardException выход из приложения из-за ошибки (обработанный)
     */
    void writeDataToFile(Map<Pair,Dep> iMap) throws HardException {
        logger.info("Writing data to file");
        try {
            String mDataToWrite = ""; // итоговая строка для записи в файл
            logger.trace("iMap = " + iMap.toString());
            logger.trace("iMap.size() = " + iMap.size());

            // если входной мап не пустой
            if (iMap.size() > 0) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.newDocument();
                Element eResults = doc.createElement("Results");
                doc.appendChild(eResults);

                // бежим по мапу, создаем поля сущностям xml
                for (Map.Entry<Pair, Dep> entry : iMap.entrySet()) {
                    Pair newPair = entry.getKey();
                    Dep newDep = entry.getValue();
                    logger.trace("newDep = " + newDep.toString());

                    Element eRow = doc.createElement("Row");
                    eResults.appendChild(eRow);

                    Element nNode = doc.createElement("DEPCODE");
                    Text tText = doc.createTextNode(newPair.getCode());
                    nNode.appendChild(tText);
                    eRow.appendChild(nNode);

                    nNode = doc.createElement("DEPJOB");
                    tText = doc.createTextNode(newPair.getJob());
                    nNode.appendChild(tText);
                    eRow.appendChild(nNode);

                    // если значение null - не создаем для него запись. иначе, если задано или пустая строка - создаем. при загрузке обратно работает по такой логике.
                    if (newDep.getDescription() != null) {
                        nNode = doc.createElement("DESCRIPTION");
                        tText = doc.createTextNode(newDep.getDescription());
                        nNode.appendChild(tText);
                        eRow.appendChild(nNode);
                    }
                }

                DOMSource domSource = new DOMSource(doc);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                StringWriter sw = new StringWriter();
                StreamResult sr = new StreamResult(sw);
                transformer.transform(domSource, sr);

                mDataToWrite = sw.toString();
            }

            logger.trace("mDataToWrite = " + mDataToWrite);

            try (PrintWriter pw = new PrintWriter(mFilename, "UTF-8")) {
                pw.println(mDataToWrite);
            } catch (IOException ioEx) {
                logger.error("Error: failed to save data to file");
                throw new HardException(ioEx);
            }
        } catch (Exception ex) {
            logger.error("Error: failed to write data to file");
            throw new HardException(ex);
        }
    }
}
