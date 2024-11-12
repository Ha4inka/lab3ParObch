package com.company;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

// Клас Manager контролює сховище та семафори для синхронізації
class Manager {
    public Semaphore access; // Дозволяє доступ лише одному потоку до сховища
    public Semaphore full;   // Дозволяє додавати до сховища лише при наявності місця
    public Semaphore empty;  // Дозволяє забирати з сховища лише при наявності продукту

    public ArrayList<String> storage = new ArrayList<>(); // Сховище для продуктів

    public Manager(int storageSize) {
        access = new Semaphore(1);         // Тільки один потік може отримати доступ до сховища одночасно
        full = new Semaphore(storageSize); // Семафор, що обмежує розмір сховища
        empty = new Semaphore(0);          // Початкове значення 0, бо сховище пусте
    }
}

// Клас Producer представляє виробника, що створює продукцію
class Producer implements Runnable {
    private final int itemNumbers; // Кількість продуктів, які виробник має створити
    private final Manager manager; // Менеджер для доступу до сховища

    public Producer(int itemNumbers, Manager manager) {
        this.itemNumbers = itemNumbers;
        this.manager = manager;
        new Thread(this).start(); // Запуск потоку для виробника
    }

    @Override
    public void run() {
        for (int i = 0; i < itemNumbers; i++) {
            try {
                manager.full.acquire();     // Очікування місця в сховищі
                manager.access.acquire();   // Блокування доступу до сховища

                manager.storage.add("item " + i); // Додавання продукту в сховище
                System.out.println("Added item " + i);

                manager.access.release();   // Звільнення доступу до сховища
                manager.empty.release();    // Семафор для споживачів, що продукт готовий
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Клас Consumer представляє споживача, що використовує продукцію
class Consumer implements Runnable {
    private final int itemNumbers; // Кількість продуктів, які споживач має забрати
    private final Manager manager; // Менеджер для доступу до сховища

    public Consumer(int itemNumbers, Manager manager) {
        this.itemNumbers = itemNumbers;
        this.manager = manager;
        new Thread(this).start(); // Запуск потоку для споживача
    }

    @Override
    public void run() {
        for (int i = 0; i < itemNumbers; i++) {
            String item;
            try {
                manager.empty.acquire();    // Очікування наявності продукту в сховищі
                Thread.sleep(1000);         // Імітація затримки для процесу споживання
                manager.access.acquire();   // Блокування доступу до сховища

                item = manager.storage.get(0); // Отримання продукту з сховища
                manager.storage.remove(0);     // Видалення продукту зі сховища
                System.out.println("Took " + item);

                manager.access.release();   // Звільнення доступу до сховища
                manager.full.release();     // Семафор для виробників, що в сховищі звільнилось місце
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

// Клас Main створює приклад з кількома виробниками та споживачами
public class Main {
    public static void main(String[] args) {
        Main main = new Main();
        int storageSize = 3;  // Розмір сховища
        int itemNumbers = 10; // Кількість продуктів, які виробники та споживачі оброблятимуть

        main.starter(storageSize, itemNumbers); // Ініціалізація програми
    }

    private void starter(int storageSize, int itemNumbers) {
        Manager manager = new Manager(storageSize); // Створення менеджера

        // Створення кількох виробників та споживачів
        new Consumer(itemNumbers, manager);
        new Consumer(itemNumbers, manager);

        new Producer(itemNumbers, manager);
        new Producer(itemNumbers, manager);
    }
}
