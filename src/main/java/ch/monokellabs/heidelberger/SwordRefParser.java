package ch.monokellabs.heidelberger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * German scripture references translated to english sword compatible keys.
 */
public class SwordRefParser
{
	private static final Pattern DE_REF = Pattern.compile("([0-9]*)\\.*\\s*([^\\s]+) ?([0-9]+)[\\, ]*([0-9]*)");

	public static String parseBibRef(String ref)
	{
		Matcher matcher = DE_REF.matcher(ref.trim());
		if (matcher.find())
		{
			String bookNo = matcher.group(1);
			String book = matcher.group(2);
			String chapter = matcher.group(3);
			String vers = matcher.group(4);
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
		    if (chapter != null)
		    {
		    	key.append(" ").append(chapter);
		    }
		    if (StringUtils.isNotBlank(vers))
		    {
		    	key.append(":").append(vers);
		    }
		    return key.toString();
		}
		else
		{
			System.err.println("not a ref: "+ref);
		}
		return ref;
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

		trans("1. Mose", "Gen");
		trans("2. Mose", "Exo");
		trans("3. Mose", "Lev");
		trans("4. Mose", "Num");
		trans("5. Mose", "Deu");
	}
}