package DataSyncApp;

/**
 * Используется при неудачном завершении работы: когда произошла предсказуемая ошибка, либо когда было отловлено ожидаемое исключение и отправлено выше.
 * При обработке исключения на каждом этапе выводится соответствующее сообщение, какой этап не удалось выполнить.
 */
public class HardException extends Exception {
    public HardException() { super(); }
    public HardException(String message) { super(message); }
    public HardException(String message, Throwable cause) { super(message, cause); }
    public HardException(Throwable cause) { super(cause); }
}
