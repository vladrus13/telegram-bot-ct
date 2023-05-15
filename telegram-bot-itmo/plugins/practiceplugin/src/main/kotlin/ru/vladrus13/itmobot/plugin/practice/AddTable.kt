package ru.vladrus13.itmobot.plugin.practice

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.model.Permission
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.google.GoogleTableResponse.Companion.getCredentials

class AddTable(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = "Пункт создания таблицы"

    override val name: String
        get() = "Создать новую таблицу (введите название)"
    override val systemName: String
        get() = "makeTable"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    /**
     * ublic static String createSpreadsheet(String title) throws IOException {
     *         /* Load pre-authorized user credentials from the environment.
     *            TODO(developer) - See https://developers.google.com/identity for
     *             guides on implementing OAuth2 for your application. */
     *     GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
     *         .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
     *     HttpRequestInitializer requstInitializer = new HttpCredentialsAdapter(
     *         credentials);
     *
     *     // Create the sheets API client
     *     Sheets service = new Sheets.Builder(new NetHttpTransport(),
     *         GsonFactory.getDefaultInstance(),
     *         requestInitializer)
     *         .setApplicationName("Sheets samples")
     *         .build();
     *
     *     // Create new spreadsheet with a title
     *     Spreadsheet spreadsheet = new Spreadsheet()
     *         .setProperties(new SpreadsheetProperties()
     *             .setTitle(title));
     *     spreadsheet = service.spreadsheets().create(spreadsheet)
     *         .setFields("spreadsheetId")
     *         .execute();
     *     // Prints the new spreadsheet id
     *     System.out.println("Spreadsheet ID: " + spreadsheet.getSpreadsheetId());
     *     return spreadsheet.getSpreadsheetId();
     *   }
     */

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val text: String = update.message.text
        val listTexts = text
            .split("\n")
            .filter(String::isNotBlank)
            .map { item -> listOf(item) };
        if (text.isBlank()) {
            unknownCommand(bot, user)
        }

        // All what I need for creating table
        val name: String = listTexts[0][0]
        val peopleList = listTexts.subList(1, listTexts.size)

        val spreadsheet = Spreadsheet()
            .setProperties(
                SpreadsheetProperties()
                    .setTitle(name)
            )
        val sheetsService = createSheetsService(name)

        val request = sheetsService.spreadsheets().create(spreadsheet)

        val response: String = request.execute().toString()

        val mapper = ObjectMapper()

        val node = mapper.readTree(response)
        val id = node.get("spreadsheetId").toString().substring(1, node.get("spreadsheetId").toString().length - 1)
        val url = node.get("spreadsheetUrl").toString()

        // I need it for writing cells

        val body = ValueRange()
            .setValues(peopleList)
        val range = "Sheet1!A1:A" + peopleList.size

        val result: UpdateValuesResponse =
            sheetsService.spreadsheets().values().update(id, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()

        user.send(
            bot = bot,
            text = url,
            replyKeyboard = getReplyKeyboard(user)
        )
    }

    companion object {
        private fun createSheetsService(text: String): Sheets {
            val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
            return Sheets.Builder(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                getCredentials(httpTransport)
            )
                .setApplicationName(text)
                .build()
        }
    }
}