# Модуль 5 — Практические задания 1–4 (Хранение файлов, MediaStore, Room, DataStore)

Студент: Начинкин Илья Сергеевич, группа ИКБО-65-23 (РТУ МИРЭА, ИИТ).

## Структура каталогов
- `task01_diary_files/` — Задание 1: Личный дневник (Internal Storage `context.filesDir`, оптимизированный список без пересканирования)
- `task02_03_photo_gallery_mediastore/` — Задания 2 и 3: Личная фотогалерея с камерой (`getExternalFilesDir`), сеткой `LazyVerticalGrid` (3 колонки), `DropdownMenu` и экспортом в общую галерею через `MediaStore`
- `task04_todolist_room_datastore/` — Задание 4: Приложение `TodoList` в Clean Architecture (`data/`, `domain/`, `presentation/`, `navigation/`) с разовым импортом JSON в SQLite через Room и сохранением настройки «Цвет завершенных» в Preferences DataStore
