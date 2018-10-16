package DataSyncApp;

import java.util.Arrays;

/**
 * Класс, инкапсулирующий поля составного ключа БД, и участвующий в обмене данными между БД и файлом.
 * Обмен данными между БД и файлом происходит посредством мап-коллекций Map({@link Pair}, {@link Dep}).
 * Экземпляры класса Pair являются ключами в этих мапах.
 * Состоит из значений строк двух полей БД - DepCode и DepJob, которые являются составным ключом.
 * Два объекта класса считаются одинаковыми, если соответствующие поля объектов совпадают.
 * Не допускается наличие двух и более одинаковых объектов класса в файле и БД.
 * При выгрузке из файла используются соответствующие теги DepCode и DepJob.
 */
class Pair {
    private String code; // DepCode
    private String job;  // DepJob

    Pair(String code, String job) {
        this.code = code;
        this.job = job;
    }

    String getCode() {
        return code;
    }

    /*public void setCode(String code) {
        this.code = code;
    }*/

    String getJob() {
        return job;
    }

    /*public void setJob(String job) {
        this.job = job;
    }*/

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{this.code, this.job});
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null) return false;

        if (getClass() != obj.getClass()) return false;

        Pair other = (Pair) obj;

        if (this.code == null) {
            if (other.code != null) return false;
        }
        else {
            if (!this.code.equals(other.code)) return false;
        }

        return ((this.job == null) ? (other.job == null) : (this.job.equals(other.job)));
    }

    @Override
    public String toString() {
        return "Pair{code='" + code + "', job='" + job + "'}";
    }
}
