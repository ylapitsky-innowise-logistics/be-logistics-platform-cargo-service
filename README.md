0️⃣ 1️⃣ 2️⃣ 3️⃣ 4️⃣ 5️⃣ 6️⃣ 7️⃣ 8️⃣ 9️⃣ 🔟 
# Getting Started

---
## Базовая информация:

### Порт приложения: **`8085`**

>### Подключение к **`Postgres`**
> - Host: **`localhost`**
> - Port: **`5433`**
> - User: **`postgres`**
> - Password: **`postgres`**
> - Database: **`logistics_platform_db`**
> - URL: **`jdbc:postgresql://localhost:5433/logistics_platform_db`**

> ### Подключение к **`Mongo`**
> - Host: **`localhost`**
> - Port: **`27018`**
> - Database: **`logistics_cargo_mongo_db`**
> - URL: **`mongodb://localhost:27018/logistics_cargo_mongo_db`**
> 
>> Аутентификация: **отключена** (для локальной разработки)
>> - User: **`admin`** - НЕ НУЖНО! // _(по умолчанию зашито в образе)_
>> - Password: **`pass`** - НЕ НУЖНО! // _(по умолчанию зашито в образе)_
>
> Порт со стандартного `27017` на нестандартный `27018`;  
> **Web**-версия, ссылка на вход: http://localhost:8888/  
> - Посмотреть изображение из БД:
> - http://localhost:8085/api/v1/catalog/images/ id_картинки

---
## Порты сервиса:

| Сервис                            | Внешний порт |
|:----------------------------------|:-------------|
| PostgreSQL                        | `5433`       |
| PostgreSQL для Keycloak           | `5434`       |
| MongoDB                           | `27018`      |
| Mongo Express                     | `8888`       |
| Keycloak                          | `8080`       |
| Cargo Service (в application.yml) | `8085`       |

---
```text
cargo_storage_db (MongoDB)
 ├── 📦 GridFS Хранилище тяжелых бинарников (Файлы разбиты на чанки)
 │    ├── fs.files      -> Метаданные файлов (имя, размер, хэш)
 │    └── fs.chunks     -> Сырые бинарные куски картинок (по 255 КБ)
 └── 📑 Обычная Коллекция документов
      └── image_metadata -> Бизнес-данные, для структурированных метаданных (комментарии, флаги брака, автор)
```

---
