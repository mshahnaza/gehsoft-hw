package org.example;

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
