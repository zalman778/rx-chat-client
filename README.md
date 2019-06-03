##№ Реактивый чат, клиент на android

Используются технологии rsocket, retrofit2, rxjava2, glide, gson, mvvm, data binding, dagger.
Все сообщения прилетают реактивно с одного клиента на другой.

## Требование

Для работы клиента необходимо
* добавить сертификат PKCS12: res/raw/keystore.p12

* добавить ключи своих сертификата: в gradle.properties добавить CLIENT_CERT_PASS = "...", CLEINT_NETTY_KEYSTORE_PASS = "..."

* добавить ip сервера: в gradle.properties добавить SERVER_IP_ADDR="..."

## Скриншоты

Активити авторизации и активити регистрации:
![Alt text](img/rx-chat-screen-02.jpg?raw=true "Активити авторизации и активити регистрации")

Активити диалога:
![Alt text](img/rx-chat-screen-01.jpg?raw=true "Активити диалога")