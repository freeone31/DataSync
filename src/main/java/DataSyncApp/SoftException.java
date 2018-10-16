package DataSyncApp;

/**
 * Используется при удачном завершении работы: когда по инициативе пользователя или по схожим причинам нужно выйти из приложения.
 * Обработка исключения не выводит никаких новых сообщений.
 */
public class SoftException extends Exception {
    public SoftException() { super(); }
    public SoftException(String message) { super(message); }
    public SoftException(String message, Throwable cause) { super(message, cause); }
    public SoftException(Throwable cause) { super(cause); }
}
