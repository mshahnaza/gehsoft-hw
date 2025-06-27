package org.example;

import java.util.ArrayList;
import java.util.List;

public class App
{
    public static void main( String[] args )
    {
        CustomList<String> list = new CustomList<>();
        list.add("A");
        System.out.println(getElementData(list));
        list.add("B");
        System.out.println(getElementData(list));
        list.add("C");
        System.out.println(getElementData(list));
        list.remove(1);
        System.out.println(getElementData(list));
        list.add(1, "D");
        System.out.println(getElementData(list));
        list.set(1, "E");
        System.out.println(getElementData(list));

        System.out.println("Contains A: " + list.contains("A")); // true
        System.out.println("Contains Z: " + list.contains("Z")); // false

        // Проверка containsAll
        List<String> toCheck = new ArrayList<>();
        toCheck.add("A");
        toCheck.add("E");
        System.out.println("Contains all [A, E]: " + list.containsAll(toCheck)); // true

        toCheck.add("Z");
        System.out.println("Contains all [A, E, Z]: " + list.containsAll(toCheck)); // false

        // Проверка addAll
        List<String> toAdd = new ArrayList<>();
        toAdd.add("X");
        toAdd.add("Y");
        list.addAll(toAdd); // добавляет в конец
        System.out.println("After addAll [X, Y]: " + getElementData(list));

        List<String> toRemove = new ArrayList<>();
        toRemove.add("X");
        toRemove.add("A"); // A был добавлен в начале

        boolean removed = list.removeAll(toRemove);
        System.out.println("removeAll [X, A] result: " + removed);
        System.out.println("After removeAll: " + getElementData(list));

// Попытка удалить элементы, которых нет
        List<String> notPresent = new ArrayList<>();
        notPresent.add("Z");
        notPresent.add("Q");

        boolean removedNone = list.removeAll(notPresent);
        System.out.println("removeAll [Z, Q] result: " + removedNone);
        System.out.println("After trying to remove [Z, Q]: " + getElementData(list));

        List<String> toRetain = new ArrayList<>();
        toRetain.add("E");
        toRetain.add("Y");

        boolean retained = list.retainAll(toRetain);
        System.out.println("retainAll [E, Y] result: " + retained); // должны остаться только E и Y
        System.out.println("After retainAll: " + getElementData(list));

        list.add("Z");

// Попытка оставить элементы, которых нет в списке
        List<String> retainNone = new ArrayList<>();
        retainNone.add("Z");

        boolean retainedNone = list.retainAll(retainNone);
        System.out.println("retainAll [Z] result: " + retainedNone); // всё должно быть удалено
        System.out.println("After retainAll [Z]: " + getElementData(list));

        list.clear();
        System.out.println("[" + getElementData(list) + "]");
    }

    public static String getElementData(CustomList list) {
        String data = "";
        for(int i = 0; i < list.size(); i++) {
            data += list.get(i).toString();
        }
        return data;
    }
}
