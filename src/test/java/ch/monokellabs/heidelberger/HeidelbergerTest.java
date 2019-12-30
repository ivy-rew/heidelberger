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
		Files.writeString(testOut, main, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	@Test
	public void sword() throws BookException, NoSuchKeyException
	{
		LocalSword sword = new LocalSword("GerSch");
		String ref = sword.getPlainText("Prov 4:23");
		System.out.println(ref);
		assertThat(ref).contains("Mehr als alles andere behüte dein Herz");
		assertThat(ref)
			.as("split verse ref and it's text")
			.startsWith("Proverbs 4:23 Mehr als alles andere behüte dein Herz");

		assertThat(sword.getPlainText("1 Joh 1:10"))
			.as("handle indexOutOfBounds in OSIS util with charm")
			.startsWith("1 John 1:10 Wenn wir sagen, wir haben nicht");
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
		assertThat(bibleRefs).hasSize(861);

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

		unresolvable.forEach(System.out::println);
		System.out.println("not resolved: "+unresolvable.size()+" out of "+bibleRefs.size());

		assertThat(unresolvable).containsOnly(
				"Job 16:35", // not in english translations > v 35 does not exist.
				"Sir 3:27" // sirach is not part of the biblical canon
		);
	}

	LocalSword sword = new LocalSword("GerSch");

	@Test
	public void refSplitMulti()
	{
		assertThat(CatechismParser.splitMultiRef("Ps 51, 20; 122, 6-7"))
			.containsExactly(
					"Ps 51, 18", // especially old testament seems to old and new verse numbers :-/
					"Ps 122, 6-7");
		assertThat(CatechismParser.splitMultiRef("Hebr 4, 2-3;10, 39"))
		.containsExactly(
				"Hebr 4, 2-3",
				"Hebr 10, 39");
		assertThat(CatechismParser.splitMultiRef("1. Kor 15, 21-22.25-26"))
			.containsExactly(
				"1. Kor 15, 21-22",
				"1. Kor 15, 25-26");
		assertThat(CatechismParser.splitMultiRef("Jes; 40, 18-20.25"))
			.containsExactly("Jes 40, 18-20", "Jes 40, 25");
		assertThat(CatechismParser.splitMultiRef("Mt 5, 21-22.26.52"))
			.containsExactly("Mt 5, 21-22", "Mt 26, 52");
		assertThat(CatechismParser.splitMultiRef("Hebr 1,1, Apg 3, 22-24; 10, 43"))
			.containsExactly("Hebr 1, 1", "Apg 3, 22-24", "Apg 10, 43");
	}

	@Test
	public void refParser() throws Exception
	{
		assertThat(lookup(SwordRef.parse("1. Joh 3, 8"))).isEqualTo("1 Joh 3:8");
		assertThat(lookup(SwordRef.parse("Röm 14, 8"))).isEqualTo("Rom 14:8");
		assertThat(lookup(SwordRef.parse("1. Mose 3"))).isEqualTo("Gen 3");
		assertThat(lookup(SwordRef.parse("Mt 10, 29-31"))).isEqualTo("Mt 10:29-31");

		assertThat(lookup(SwordRef.parse("Das 1. Gebot"))).isEqualTo("Deu 5:7");
		assertThat(lookup(SwordRef.parse("Röm3, 20"))).isEqualTo("Rom 3:20");
		assertThat(lookup(SwordRef.parse("1. Petr.1, 2"))).isEqualTo("1 Pet 1:2");
	}

	private String lookup(SwordRef parseBibRef) throws NoSuchKeyException {
		sword.book.getKey(parseBibRef.enKey());
		return parseBibRef.enKey();
	}

}