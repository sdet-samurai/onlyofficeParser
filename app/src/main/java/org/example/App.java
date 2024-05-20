package org.example;


import com.codeborne.selenide.*;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.url;

public class App {
    private static final String PATH_NAME;
    private static final String FILE_NAME;
    private static final List<String> CSV_HEADERS;

    static {
        PATH_NAME = new AppConfig("config.properties").getProperty("path_name");
        FILE_NAME = new AppConfig("config.properties").getProperty("csv_file_name");
        CSV_HEADERS = List.of("Country", "CompanyName", "FullAddress");
        Configuration.browser = "firefox";
        Configuration.pageLoadStrategy = "eager";
        Configuration.browserSize = "1920x1080";
        Configuration.headless = true;
    }

    public static void main(String[] args) throws IOException {
        FileWriter fileWriter = new FileWriter(createDirAndOtputFile());
        ICSVWriter writer = new CSVWriterBuilder(fileWriter)
                .withSeparator(';')
                .build();
        writer.writeNext(CSV_HEADERS.toArray(new String[0]));

        openPage();
        processAddresses(writer);

        WebDriverRunner.closeWebDriver();
        writer.close();
    }

    private static void processAddresses(ICSVWriter writer) {
        ElementsCollection addresses = $$("div[itemtype='https://schema.org/PostalAddress']");
        List<String> elems = new ArrayList<>(3);
        for (SelenideElement selenideElement : addresses) {
            elems.add(selenideElement.$(".region").getText());
            elems.add(selenideElement.$("span b").getText());
            elems.add(getFullAddress(selenideElement));
            writer.writeNext(elems.toArray(new String[0]));
            elems.clear();
        }
    }

    private static void openPage() {
        open("https://www.onlyoffice.com");
        $("#navitem_about").hover();
        $("#navitem_about_contacts").shouldBe(Condition.visible);
        $("#navitem_about_contacts").click();

        webdriver().shouldHave(
                url("https://www.onlyoffice.com/contacts.aspx"),
                Duration.ofSeconds(40));
        $$("div[itemtype='https://schema.org/PostalAddress']")
                .shouldHave(CollectionCondition.sizeGreaterThanOrEqual(8),
                        Duration.ofSeconds(20));
    }

    private static String getFullAddress(SelenideElement element) {
        StringBuilder fullAddress = new StringBuilder();
        if (element.$("span[itemprop=streetAddress]").exists()) {
            fullAddress.append(element.$("span[itemprop=streetAddress]").getText());
        }
        fullAddress.append(element.$("span[itemprop=addressCountry]").getText());
        if (element.$("span[itemprop=postalCode]").exists()) {
            fullAddress.append(element.$("span[itemprop=postalCode]").getText());
        }
        if (element.$("span[itemprop=telephone] a").exists()) {
            fullAddress.append("Phone: ")
                    .append(element.$("span[itemprop=telephone] a").getText());
        }
        return fullAddress.toString();
    }

    private static File createDirAndOtputFile() throws IOException {
        File directory = new File(PATH_NAME);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, FILE_NAME);
        file.createNewFile();
        return file;
    }
}
