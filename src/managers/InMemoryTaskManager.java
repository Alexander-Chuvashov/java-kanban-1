package managers;


import task.Epic;
import task.Subtask;
import task.Task;
import status.Stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks; //Объявление задач
    private final HashMap<Integer, Epic> epics; //Объявление мап для эпик
    private final HashMap<Integer, Subtask> subtasks; //Объявления мап для подзадач
    private final HistoryManager historyManager;

    // Инициализация мап
    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();

    }

    private int nextID = 1;

    // Методы
    //Добавить задачу
    @Override
    public Task addTask(Task task) {
        task.setId(getNextID());
        tasks.put(task.getId(), task);
        return task;
    }

    //Добавить эпик
    public Epic addEpic(Epic epic) {
        epic.setId(getNextID());
        epics.put(epic.getId(), epic);
        return epic;
    }

    //Добавить подзадачу
    public Subtask addSubtask(Subtask subtask) {
        subtask.setId(getNextID());
        Epic epic = epics.get(subtask.getEpicID());
        epic.addSubtask(subtask);
        subtasks.put(subtask.getId(), subtask);
        updateEpicStat(epic);
        return subtask;
    }

    // Методы для получения
    @Override
    public List<Task> getTasks() { return new ArrayList<>(tasks.values()); }

    @Override
    public List<Epic> getEpics() { return new ArrayList<>(epics.values()); }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtasksList();
    }


    // Методы для удаления
    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic :epics.values()) {
            epic.clearSubtasks();
            epic.setStat(Stat.NEW);
        }
    }

    @Override
    public Task deleteTaskById(int id) {
        return tasks.remove(id);
    }

    @Override
    public Subtask deleteSubtaskById() {
        return null;
    }

    @Override
    public Epic deleteEpicById(int id) {
        ArrayList<Subtask> epicSubtasks = epics.remove(id).getSubtasksList();
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
        return epics.remove(id);
    }


    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        int epicId = subtask.getEpicID();
        subtasks.remove(id);
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtaskList = epic.getSubtasksList();
        subtaskList.remove(subtask);
        epic.setSubtasksList(subtaskList);
        updateEpicStat(epic);
    }

    public int getNextId() {
        return 0;
    }

    // Методы для обновления
    public Task updateTask(Task task) {
        Integer taskID = task.getId();
        if (!tasks.containsKey(taskID)) return null;
        tasks.replace(taskID, task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        Integer epicId = epic.getId();
        if (!epics.containsKey(epicId)) return null;
        Epic oldEpic = epics.get(epicId);
        ArrayList<Subtask> oldEpicSubtaskList = oldEpic.getSubtasksList();
        if (!oldEpicSubtaskList.isEmpty()) {
            for (Subtask subtask : oldEpicSubtaskList) {
                subtasks.remove(subtask.getId());
            }
        }
        epics.replace(epicId, epic);
        ArrayList<Subtask> newEpicSubtaskList = epic.getSubtasksList();
        if (!newEpicSubtaskList.isEmpty()) {
            for (Subtask subtask : newEpicSubtaskList) {
                subtasks.put(subtask.getId(), subtask);
            }
        }
        updateEpicStat(epic);
        return epic;
    }

    public Subtask updateSubtask(Subtask subtask) {
        Integer subtaskId = subtask.getId();
        if (!subtasks.containsKey(subtaskId)) {
            return null;
        }
        int epicID = subtask.getEpicID();
        Subtask oldenSubtask = subtasks.get(subtaskId);
        subtasks.replace(subtaskId, subtask);
        Epic epic = epics.get(epicID);
        ArrayList<Subtask> subtaskList = epic.getSubtasksList();
        subtaskList.remove(oldenSubtask);
        subtaskList.add(subtask);
        epic.setSubtasksList(subtaskList);
        updateEpicStat(epic);
        return subtask;
    }

    //получить задачу по id
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    //получить эпик по id
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    //получить подзадачу по id
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    //Получаем историю
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // обновление статистики эпика
    private void updateEpicStat(Epic epic) {
        int allIsDoneCount = 0;
        int allIsNewCount = 0;
        ArrayList<Subtask> list = epic.getSubtasksList();
        for (Subtask subtask: list) {
            if (subtask.getStat() == Stat.DONE) {
                allIsDoneCount++;
            }
            if (subtask.getStat() == Stat.NEW) {
                allIsNewCount++;
            }

        }
        if (allIsDoneCount == list.size()) {
            epic.setStat(Stat.DONE);
        } else if (allIsNewCount == list.size()) {
            epic.setStat(Stat.NEW);
        } else {
            epic.setStat(Stat.IN_PROGRESS);
        }
    }

    //ПОлучение следующего id
    private int getNextID() {
        return nextID++;
    }

}


