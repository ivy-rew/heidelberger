package ch.monokellabs.heidelberger;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.jsoup.nodes.Element;

import ch.monokellabs.heidelberger.CatechismParser.RefHandler;

public class InlineResolvedFootnotes implements RefHandler
{
	private final LocalSword sword = new LocalSword("GerSch");

	@Override
	public void transform(Element bibRef, Stream<String> refs) {
		Element refWrapper = bibRef.ownerDocument().createElement("pre");
		String md = toMarkdownFootnotes(refs);
		refWrapper.appendText(md);
		bibRef.replaceWith(refWrapper);
	}

	private String toMarkdownFootnotes(Stream<String> refs) {
		Map<String, String> footNotes = new HashMap<>();
		refs.forEach(refRaw ->
		{
			String verse = refRaw.trim();
			String refKey = StringUtils.deleteWhitespace(verse);
			footNotes.put(refKey, getSwordText(verse));
		});

		String footNotePartMd = toMarkdownNotes(footNotes);
		return footNotePartMd;
	}

	private static String toMarkdownNotes(Map<String, String> footNotes) {
//		<pre>
//		[^forMd], [^forMd2]
//
//		[^forMd]: tatatata
//		[^forMd2]: tututut
//		</pre>
		String footRefsMd = footNotes.keySet().stream()
				.map(key -> "[^"+key+"]")
				.collect(Collectors.joining(", "));
		String footNootesMd = footNotes.entrySet().stream()
			.map(entry -> "[^"+entry.getKey()+"]: "+entry.getValue())
			.collect(Collectors.joining("\n"));
		String footNotePartMd = footRefsMd+"\n\n"+footNootesMd;
		return footNotePartMd;
	}

	private String getSwordText(String verse) {
		try {
			SwordRef parsed = SwordRef.parse(verse);
			if (parsed != null)
			{
				return sword.getPlainText(parsed.enKey());
			}
		} catch (BookException | NoSuchKeyException | IllegalArgumentException e) {
			System.out.println("failed to lookup: "+verse +" /ex="+e);
		}
		return verse; //unresolved; keep it for manual resolution
	}
}