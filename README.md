![Version](https://img.shields.io/badge/Bot%20Version-1.1.1-blue)
![GitHub repo size](https://img.shields.io/github/repo-size/marensovich/PoliceCheckerBot)
![GitHub watchers](https://img.shields.io/github/watchers/marensovich/PoliceCheckerBot)
[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://github.com/marensovich/PoliceCheckerBot/blob/main/LICENSE)

# Описание проекта

Этот бот предназначен для оставления записи о посте ДПС, а последствии и получения информации другим лицом о постах, находящимся рядом с ним. 
Работа бота напрямую зависит от активности пользователей.

## Автор

- [@marensovich](https://www.github.com/marensovich)

## Запуск бота

### Инициализция и запуск бота

Скачайте проект
```bash
  git clone https://github.com/marensovich/PoliceCheckerBot
```

Перейдите в директорию проекта
```bash
  cd my-project
```

Запустите бота
```bash
  chmod +x build-and-run.sh
  ./build-and-run.sh
```


## Зависимости
Для запуска проекта вам требуется доступ к [YandexMaps Static API](https://yandex.ru/maps-api/products/static-api)

Также необходимо иметь установленный Docker версии 28.3 и выше

## Переменные окружения
Для запуска этого проекта вам потребуется добавить следующие переменные окружения в файл .env:

```
YANDEX_MAPS_API_KEY
TELEGRAM_BOT_USERNAME
TELEGRAM_BOT_TOKEN
```
Подробнее в файле `.env.example`

## Возможности

- Работа с картами мира
- Предпросмотр в постов в реальном времени


## Помощь

Для получения помощи по проекту вы можете обратиться в группу [Telegram](t.me/son_of_dev228)

## Обратная связь

Если у вас есть какие-либо отзывы, пожалуйста, напишите мне в группу [Telegram](t.me/son_of_dev228) 

