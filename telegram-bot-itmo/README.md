# Welcome to telegram-bot-itmo

### It's documentation with Q&A

#### How can I launch the bot?

It's not so hard as you think. Take this algorithm:

1) Go to @BotFather (in telegram) and make new bot
2) Go to telegram-bot-itmo/src/main/resources and rename data
   table_TEMPLATE.properties to datatable.properties
   same with main_TEMPLATE.properties
3) Paste token from @BotFather to main.properties and paste id of spreadsheet to MAIN_TABLE, and link to SCHEDULE_TABLE
4) In database.properties, I chose postgresql db and launched it with this command 
```bash
docker-compose up --build
```
 but you can choose another if you want
   <br/>
   My example fill:

* url=jdbc:postgresql://localhost:5432/${DB_NAME}
* driver=org.postgresql.Driver
* user=${USERNAME}
* password=${PASSWORD}
5) Also you need to get correct credentials.json. You may get it from [link](https://developers.google.com/sheets/api/quickstart/java)
6) Then launch it with run.sh 
