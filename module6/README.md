# Модуль 6 — Практические задания 1–7 (Retrofit, Ktor Client/Server, PostgreSQL/Neon, JWT, BLE)

Студент: Начинкин Илья Сергеевич, группа ИКБО-65-23 (РТУ МИРЭА, ИИТ).
Тема проекта: «Разработка клиент-серверного мобильного приложения "Справочник фильмов"».

## Структура каталогов
- `task01_photo_catalog_retrofit/` — Задание 1: Приложение «Фотокаталог» (Retrofit + Clean Architecture + Coil + скачивание через SAF)
- `task02_nobel_ktor_client/` — Задание 2: Приложение «Нобелевские лауреаты» (Ktor Client + Clean Architecture + фильтры по году/категории)
- `task03_auth_dummyjson/` — Задание 3: Приложение «Авторизация» (DummyJSON API + Bearer Token в Preferences DataStore)
- `task04_ktor_server_nobel_jwt/` — Задание 4: Серверное приложение на Ktor «Nobel Prize API» (Clean Architecture + JWT 30 мин + CallLogging + in-memory)
- `task05_ktor_server_neon_postgres/` — Задание 5: Доработка Ktor-сервера с PostgreSQL (Neon.tech), Exposed, HikariCP, таблицами `users`/`prizes`/`laureates`/`user_prizes` и OpenAPI/Redocly
- `task06_nobel_client_own_server/` — Задание 6: Доработка клиента из Задания 2 для работы с собственным Ktor-сервером из Задания 5 (авторизация + избранные премии)
- `task07_ble_heart_rate/` — Задание 7: BLE Android-приложение «Монитор сердечного ритма» (Heart Rate Service `0x180D`, Characteristic `0x2A37`, CCCD `0x2902`)
- `bonus_film_directory_ktor_server/` — Адаптация клиент-серверного стека Модуля 6 (Ktor + Exposed + HikariCP + PostgreSQL Neon) для темы «Справочник фильмов»
