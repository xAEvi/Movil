package com.example.logintarea;

public class Routine {
    private long id;
    private String name;

    public Routine(long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor sin ID para cuando se crea una nueva rutina antes de insertarla
    public Routine(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() { // Útil para debug
        return "Routine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}