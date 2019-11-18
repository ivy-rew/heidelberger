package ch.monokellabs.heidelberger;

import java.util.Iterator;

import org.crosswire.common.xml.SAXEventProvider;
import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.book.BookCategory;
import org.crosswire.jsword.book.BookData;
import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.book.OSISUtil;
import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.crosswire.jsword.passage.Passage;

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
		return OSISUtil.getPlainText(data.getOsisFragment());
	}

	 /**
     * Obtain a SAX event provider for the OSIS document representation of one or more book entries.
     *
     * @param bookInitials the book to use
     * @param reference a reference, appropriate for the book, of one or more entries
     */
    private SAXEventProvider getOSIS(String reference, int maxKeyCount) throws BookException, NoSuchKeyException
    {
        if (reference == null)
        {
            return null;
        }

        Key key = null;
        if (BookCategory.BIBLE.equals(book.getBookCategory()))
        {
            key = book.getKey(reference);
            ((Passage) key).trimVerses(maxKeyCount);
        }
        else
        {
            key = book.createEmptyKeyList();

            @SuppressWarnings("unchecked")
			Iterator<Key> iter = book.getKey(reference).iterator();
            int count = 0;
            while (iter.hasNext())
            {
                if (++count >= maxKeyCount)
                {
                    break;
                }
                key.addAll((Key) iter.next());
            }
        }

        BookData data = new BookData(book, key);
        return data.getSAXEventProvider();
    }

}
