package ch.monokellabs.heidelberger;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

public class HeidelbergerTest
{

	@Test
	public void download() throws IOException, InterruptedException
	{
		assertThat(CatechismLoader.getCatechismHtml()).isNotEmpty();
	}

	@Test
	public void extract() throws IOException, InterruptedException
	{
		String html = CatechismLoader.getCatechismHtml();
		String main = new CatechismParser(html).getMain();
		assertThat(main).isNotEmpty();
		Path testOut = new File("target/extract.html").toPath();
		Files.writeString(testOut, main, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}
}