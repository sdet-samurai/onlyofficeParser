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
    static {
        Configuration.browser = "firefox";
        Configuration.pageLoadStrategy = "eager";
        Configuration.browserSize = "1920x1080";
        Configuration.headless = true;
    }

    public static void main(String[] args) throws IOException {
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

        File file = new File("output", "addresses.csv");
        file.createNewFile();
        ICSVWriter writer = new CSVWriterBuilder(new FileWriter(file))
                .withSeparator(';')
                .build();
        ElementsCollection addresses = $$("div[itemtype='https://schema.org/PostalAddress']");
        List<String> elems = new ArrayList<>(3);
        for (SelenideElement selenideElement : addresses) {
            elems.add(selenideElement.$(".region").getText());
            elems.add(selenideElement.$("span b").getText());

            StringBuilder fullAddress = new StringBuilder();
            if (selenideElement.$("span[itemprop=streetAddress]").exists()) {
                fullAddress.append(selenideElement.$("span[itemprop=streetAddress]").getText());
            }
            fullAddress.append(selenideElement.$("span[itemprop=addressCountry]").getText());
            if (selenideElement.$("span[itemprop=postalCode]").exists()) {
                fullAddress.append(selenideElement.$("span[itemprop=postalCode]").getText());
            }
            if (selenideElement.$("span[itemprop=telephone] a").exists()) {
                fullAddress.append("Phone: ")
                        .append(selenideElement.$("span[itemprop=telephone] a").getText());
            }
            elems.add(fullAddress.toString());

            writer.writeNext(elems.toArray(new String[0]));
            elems.clear();
        }
        WebDriverRunner.closeWebDriver();
        writer.close();
    }
}
