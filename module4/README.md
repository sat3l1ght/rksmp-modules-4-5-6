# Модуль 4 — Практические задания 1–14 (Kotlin / Android Jetpack Compose)

Студент: Начинкин Илья Сергеевич, группа ИКБО-65-23 (РТУ МИРЭА, ИИТ).

## Структура каталогов
- `task01_parallel_loader/` — Задание 1: Параллельная загрузка 3 JSON-источников (Coroutines, `async`/`await`, обработка сбоев)
- `task02_json_duplicates_sha256/` — Задание 2: Поиск дубликатов JSON-файлов по SHA-256 с таймаутом (`withTimeoutOrNull`)
- `task03_github_search_debounce/` — Задание 3: Android-экран поиска репозиториев GitHub с `debounce` и `Job.cancel()`
- `task04_social_feed/` — Задание 4: Социальная лента с параллельной загрузкой аватарок и комментариев (`supervisorScope`)
- `task05_foreground_counter/` — Задание 5: Foreground Service — счётчик времени с уведомлением
- `task06_background_timer/` — Задание 6: Background Service — одноразовый таймер с уведомлением
- `task07_bound_random/` — Задание 7: Bound Service — генератор случайных чисел 0..100 каждую секунду
- `task08_workmanager_photo_chain/` — Задание 8: WorkManager — последовательная цепочка обработки фото
- `task09_workmanager_weather_parallel/` — Задание 9: WorkManager + Notifications — параллельный сбор погоды
- `task10_location_geocoder/` — Задание 10: Геолокация (`FusedLocationProviderClient`) и обратное геокодирование (`Geocoder`)
- `task11_alarm_pill_reminder/` — Задание 11: `AlarmManager` + `BroadcastReceiver` (`BOOT_COMPLETED`) — напоминание о таблетке в 20:00
- `task12_cold_flow_animal_facts/` — Задание 12: Cold Flow — генератор фактов о животных
- `task13_stateflow_currency/` — Задание 13: StateFlow — живой курс валют USD/RUB
- `task14_sensor_compass/` — Задание 14: Сенсоры (`SensorManager`) — компас с анимированной стрелкой
