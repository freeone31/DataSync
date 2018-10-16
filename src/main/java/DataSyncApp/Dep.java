package DataSyncApp;

import java.util.Arrays;

/**
 * Класс, инкапсулирующий поля строки БД (кроме id), и участвующий в обмене данными между БД и файлом.
 * Обмен данными между БД и файлом происходит посредством мап-коллекций Map({@link Pair}, {@link Dep}).
 * Экземпляры класса Dep являются значениями в этих мапах.
 * Состоит из экземпляра класса Pair (в котором хранятся поля DepCode и DepJob - уникальный составной ключ строки), и поля БД Description.
 * Два объекта класса считаются одинаковыми, если у них совпадают их ключи (Pair), и Description.
 * Не допускается наличие двух и более одинаковых объектов класса в файле и БД.
 * Объект, у которого совпадают ключи Pair, но отличаются Description, считает измененным.
 * При выгрузке из файла используются соответствующие теги DepCode, DepJob и Description.
 */
class Dep {
    private Pair pair;
    private String description; // Description

    Dep(Pair pr, String description) {
        this.pair = pr;
        this.description = description;
    }

    /*public Dep(String code, String job, String description) {
        this.pair = new Pair(code, job);
        this.description = description;
    }*/

    Pair getPair() {
        return pair;
    }

    /*public void setPair(Pair pr) {
        this.pair = pr;
    }*/

    String getDescription() {
        return description;
    }

    /*public void setDescription(String description) {
        this.description = description;
    }*/

    @Override
    public String toString() {
        return "Dep{pair='" + pair + "', description='" + description + "'}";
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[]{this.pair, this.description});
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null) return false;

        if (getClass() != obj.getClass()) return false;

        Dep other = (Dep) obj;

        if (this.description == null) {
            if (other.description != null) return false;
        }
        else {
            if (!this.description.equals(other.description)) return false;
        }

        return this.getPair().equals(other.getPair());
    }
}
