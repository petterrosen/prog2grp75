package se.su.inlupp;

import java.util.Objects;


public class T {

    private final String name;

    public T(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public  int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof T city) {
            return name.equals(city.name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
