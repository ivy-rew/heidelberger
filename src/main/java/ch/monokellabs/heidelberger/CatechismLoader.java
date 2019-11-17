package ch.monokellabs.heidelberger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class CatechismLoader
{
    public static String getCatechismHtml() throws IOException, InterruptedException
    {
    	File cached = new File("target", "catechism.html");
    	if (cached.exists()){
    		return Files.readString(cached.toPath());
    	}
    	else
    	{
    		String html = downloadCatechism();
    		Files.writeString(cached.toPath(), html, StandardOpenOption.CREATE_NEW);
    		return html;
    	}
    }

	private static String downloadCatechism() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.heidelberger-katechismus.net/Heidelberger_Katechismus___Der_gesamte_Text-8261-0-227-50.html"))
                .build();
		HttpClient client = HttpClient.newHttpClient();
		return client.send(request, BodyHandlers.ofString()).body();
	}
}
