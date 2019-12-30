package ch.monokellabs.heidelberger;

import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.book.BookData;
import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.book.OSISUtil;
import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.passage.NoSuchKeyException;

public class LocalSword {

	public final Book book;

	public LocalSword(String bookInitials)
	{
		this.book = getBook(bookInitials);
	}

	private static Book getBook(String bookInitials)
    {
        return Books.installed().getBook(bookInitials);
    }

	public String getPlainText(String reference) throws BookException, NoSuchKeyException
	{
		if (book == null) {
			return "";
		}
		Key key = book.getKey(reference);
		BookData data = new BookData(book, key);
		String fullRef = key.getName();
		try
		{
			String text = OSISUtil.getCanonicalText(data.getOsisFragment());
			return fullRef + " " + text;
		}
		catch (StringIndexOutOfBoundsException ex)
		{
			String unseparated = OSISUtil.getPlainText(data.getOsisFragment());
			int keyOffset = key.getName().length();
			String repaired = fullRef + " " + unseparated.substring(keyOffset);
			return repaired;
		}
	}

}
