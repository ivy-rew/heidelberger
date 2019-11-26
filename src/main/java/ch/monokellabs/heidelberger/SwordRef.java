package ch.monokellabs.heidelberger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * German scripture references
 */
public class SwordRef
{
	private static final Pattern DE_REF = Pattern.compile("([0-9]*)\\.*\\s*([A-Za-zöäü]+)\\.?[\\s]*([0-9]+)[\\, ]*([0-9\\-]*)");
	private static final Pattern COMMANDMENT = Pattern.compile("Das ([0-9]*)\\. Gebot");


	private final String bookNo;
	private final String book;
	public final int chapter;
	private final String verseRange;

	public static SwordRef parseOrNull(String ref)
	{
		try
		{
			return parse(ref);
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
	}

	public static SwordRef parse(String ref) throws IllegalArgumentException
	{
		Matcher matcher = COMMANDMENT.matcher(ref.trim());
		if (matcher.find())
		{
			Integer no = Integer.parseInt(matcher.group(1));
			return new SwordRef("", "Deu", 5, COMMAND_NO_TO_VERSE.get(no));
		}

		matcher = DE_REF.matcher(ref.trim());
		if (matcher.find())
		{
			String bookNo = matcher.group(1);
			String book = matcher.group(2);
			int chapter = Integer.parseInt(matcher.group(3));
			String vers = matcher.group(4);
			return new SwordRef(bookNo, book, chapter, vers);
		}
		throw new IllegalArgumentException("not a ref: "+ref);
	}

	public SwordRef(String bookNo, String book, int chapter, String verseRange)
	{
		this.bookNo = bookNo;
		this.book = book;
		this.chapter = chapter;
		this.verseRange = verseRange;
	}

	public String enKey()
	{
		StringBuilder key = new StringBuilder();
	    if (StringUtils.isNotBlank(bookNo))
	    {
	    	key.append(bookNo).append(" ");
	    }
	    String bookEn = DE_EN.getOrDefault(book, book);
	    if (book.equals("Mose"))
	    {
	    	key = new StringBuilder();
	    	bookEn = DE_EN.getOrDefault(bookNo+". "+book, book);
	    }
	    key.append(bookEn);
	    key.append(" ").append(chapter);
	    if (StringUtils.isNotBlank(verseRange))
	    {
	    	key.append(":").append(verseRange);
	    }
	    return key.toString();
	}

	public String getBook()
	{
		if (StringUtils.isNotEmpty(bookNo))
		{
			return bookNo + ". " + book;
		}
		return book;
	}

	private static final Map<String, String> DE_EN = new HashMap<>();
	private static void trans(String deBook, String enBook)
	{
		DE_EN.put(deBook, enBook);
	}
	static
	{
		trans("Röm", "Rom");
		trans("Kor", "Cor");
		trans("Kol", "Col");
		trans("Apg", "Act");
		trans("Jak", "Jam");
		trans("Offb", "Rev");
		trans("Jes", "Isa");
		trans("Spr", "Proverbs");
		trans("Hiob", "Job");
		trans("Hes", "Eze");
		trans("Pred", "Ecc");
		trans("Kön", "Ki");
		trans("Sach", "Zec");
		trans("Petr", "Pet");

		trans("1. Mose", "Gen");
		trans("2. Mose", "Exo");
		trans("3. Mose", "Lev");
		trans("4. Mose", "Num");
		trans("5. Mose", "Deu");
	}

	private static final Map<Integer, String> COMMAND_NO_TO_VERSE = new HashMap<>();
	private static void command(int no, String verse)
	{
		COMMAND_NO_TO_VERSE.put(no, verse);
	}
	static {
		command(1, "7");
		command(2, "8-10");
		command(3, "11");
		command(4, "12-15");
		command(5, "16");
		command(6, "17");
		command(7, "18");
		command(8, "19");
		command(9, "20");
		command(10, "21");
	}


}