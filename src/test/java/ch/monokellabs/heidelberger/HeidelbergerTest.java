package ch.monokellabs.heidelberger;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.passage.NoSuchKeyException;
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

	@Test
	public void sword() throws BookException, NoSuchKeyException
	{
		LocalSword sword = new LocalSword();
		String ref = sword.getPlainText("GerSch", "Prov 4:23");
		System.out.println(ref);
		assertThat(ref).contains("Mehr als alles andere behüte dein Herz");
	}
}