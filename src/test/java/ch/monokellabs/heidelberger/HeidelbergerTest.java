package ch.monokellabs.heidelberger;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.passage.Key;
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
		LocalSword sword = new LocalSword("GerSch");
		String ref = sword.getPlainText("Prov 4:23");
		System.out.println(ref);
		assertThat(ref).contains("Mehr als alles andere behüte dein Herz");
	}

	@Test
	public void germanSword() throws NoSuchKeyException
	{
		LocalSword sword = new LocalSword("GerSch");
		sword.book.getLanguage();
		Key peter = sword.book.getKey("1. Petrus 4:1");
		assertThat(peter.getName()).isEqualTo("1 Peter 4:1");
	}

	@Test
	public void resolveAllRefs() throws IOException, InterruptedException
	{
		String html = CatechismLoader.getCatechismHtml();
		List<String> bibleRefs = new CatechismParser(html).getAllRefs();
		assertThat(bibleRefs).hasSizeGreaterThan(809);

		LocalSword sword = new LocalSword("GerSch");
		List<String> unresolvable = bibleRefs.stream().map(ref -> {
			try
			{
				sword.book.getKey(ref);
				return null;
			}
			catch (Exception ex)
			{
				return ref;
			}
		})
		.filter(Objects::nonNull)
		.collect(Collectors.toList());

		System.out.println("not resolved: "+unresolvable.size()+" out of "+bibleRefs.size());

		assertThat(unresolvable).hasSizeLessThan(27);
	}

	LocalSword sword = new LocalSword("GerSch");

	@Test
	public void refSplitMulti()
	{
		assertThat(CatechismParser.splitMultiRef("Ps 51, 20; 122, 6-7"))
			.containsExactly(
					"Ps 51, 20",
					"Ps 122, 6-7");
		assertThat(CatechismParser.splitMultiRef("Hebr 4, 2-3;10, 39"))
		.containsExactly(
				"Hebr 4, 2-3",
				"Hebr 10, 39");



	}

	@Test
	public void refParser() throws Exception
	{
		assertThat(lookup(SwordRef.parse("1. Joh 3, 8"))).isEqualTo("1 Joh 3:8");
		assertThat(lookup(SwordRef.parse("Röm 14, 8"))).isEqualTo("Rom 14:8");
		assertThat(lookup(SwordRef.parse("1. Mose 3"))).isEqualTo("Gen 3");
	}

	private String lookup(SwordRef parseBibRef) throws NoSuchKeyException {
		sword.book.getKey(parseBibRef.enKey());
		return parseBibRef.enKey();
	}

}