package ch.monokellabs.heidelberger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class CatechismLoader
{
    public static String getCatechismHtml() throws IOException, InterruptedException
    {
    	File cached = new File(getCacheDir(), "catechism.html");
    	if (cached.exists()){
    		return Files.readString(cached.toPath());
    	}
    	else
    	{
    		String html = downloadCatechism();
    		cached.getParentFile().mkdirs();
    		Files.writeString(cached.toPath(), html, StandardOpenOption.CREATE_NEW);
    		System.out.println("caching catechism in "+cached);
    		return html;
    	}
    }

	private static File getCacheDir() {
		return new File(System.getProperty("java.io.tmpdir"), "heidelberger");
	}

	private static String downloadCatechism() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.heidelberger-katechismus.net/Heidelberger_Katechismus___Der_gesamte_Text-8261-0-227-50.html"))
                .build();
		HttpClient client = HttpClient.newHttpClient();
		return client.send(request, BodyHandlers.ofString()).body();
	}

	public static File getCatechismImage(File cacheDir) throws IOException, InterruptedException
	{
		File cover = new File(cacheDir, "Heidelberger_Katechismus_1563.jpg");
		if (!cover.exists())
		{
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://reformedforum.org/wp-content/blogs.dir/1/files/2012/03/Heidelberger_Katechismus_1563.jpg"))
					.build();
			HttpClient client = HttpClient.newHttpClient();
			client.send(request, BodyHandlers.ofFile(cover.toPath(),
					StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)).body();
		}
		return cover;
	}

	public static File getCatechismImage() throws IOException, InterruptedException {
		return getCatechismImage(getCacheDir());
	}
}
