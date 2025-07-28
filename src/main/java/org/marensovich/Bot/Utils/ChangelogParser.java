package org.marensovich.Bot.Utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangelogParser {

    static {
        LoggerUtil.logInfo(LoggerUtil.class, "ChangelogParser initialized");
    }

    private static final String GET_CHANGELOG_URL =
            "https://raw.githubusercontent.com/marensovich/PoliceCheckerBot/main/CHANGELOG.md";

    private static final String CHANGELOG_URL =
            "https://github.com/marensovich/PoliceCheckerBot/blob/main/CHANGELOG.md";

    public static String getChangelogUrl() {
        return CHANGELOG_URL;
    }

    public static String fetchChangelog() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(GET_CHANGELOG_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка при запросе: " + response.code());
            }
            return response.body().string();
        }
    }

    public static String getChangesForVersion(String fullChangelog, String version) {
        String cleanVersion = version.replaceAll("[^\\d.]", "");

        if (cleanVersion.isEmpty() || !cleanVersion.matches("\\d+(?:\\.\\d+)*")) {
            return "Неверный формат версии: " + version;
        }

        String versionPattern = Pattern.quote(cleanVersion);
        String regex = String.format(
                "(^## \\[v\\.%s\\] - \\d{2}\\.\\d{2}\\.\\d{4}$[\\s\\S]+?)(?=^## \\[v\\.|\\z)",
                versionPattern
        );

        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fullChangelog);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Изменения для версии v." + cleanVersion + " не найдены.";
    }
}