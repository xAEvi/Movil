package com.example.logintarea;

public class UserSelectedRoutine {
    private long id; // ID de la tabla user_selected_routines
    private long userId;
    private long routineId;
    private String routineName; // Para mostrar en la checklist
    private boolean isCompleted;

    public UserSelectedRoutine(long id, long userId, long routineId, String routineName, boolean isCompleted) {
        this.id = id;
        this.userId = userId;
        this.routineId = routineId;
        this.routineName = routineName;
        this.isCompleted = isCompleted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getRoutineId() {
        return routineId;
    }

    public void setRoutineId(long routineId) {
        this.routineId = routineId;
    }

    public String getRoutineName() {
        return routineName;
    }

    public void setRoutineName(String routineName) {
        this.routineName = routineName;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}